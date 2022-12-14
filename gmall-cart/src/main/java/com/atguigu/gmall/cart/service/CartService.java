package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.exception.CartException;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: Guan FuQing
 * @Date: 2022/12/25 22:04
 * @Email: moumouguan@gmail.com
 */
@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private CartSyncService cartSyncService;

    /**
     * 前缀
     */
    private static final String KEY_PREFIX = "CART:INFO:";

    private static final String PRICE_PREFIX = "CART:PRICE:";

    public void saveCart(Cart cart) {

        // 1. 获取登陆状态
//        UserInfo userInfo = LoginInterceptor.getUserInfo();
        // 首先获取 userKey, 赋值给 userId. 判断 userId 是否为空. 如果不为空则 把真正的 userId 赋值给 userId 变量. 保证用户信息用不为空
//        String userId = userInfo.getUserKey(); // 不管是否登陆 userKey 使用存在
//        if (userInfo.getUserId() == null) { // 判断 userInfo 中的 userId 是否为空
//            userId = userInfo.getUserId().toString();
//        }

        String userId = getUserId(); // 获取登陆状态

        /**
         * 2. 判断当前购物车是否包含该商品
         *      如果使用 opsForHash(). 不够方便, 每次都需要 外层的 key + 内层的 key 操作
         *          查询所有购物车 通过 .entries() 传入外层的 key 即可: Map<HK, HV> entries(H var1);
         *          获取购物车的某一个数据 通过 .get() 传入外层的 key + 内层的 key 即可: HV get(H var1, Object var2);
         *          判断是否包含某一条记录 通过 .hasKey 传入外层的 key + 内层的 key 判断: Boolean hasKey(H var1, Object var2);
         *              判断该用户是否包含该记录
         *          新增一条购物车记录 通过 .put() 传入 外层的 key + 内层的 key + 内层的 value 即可: void put(H var1, HK var2, HV var3);
         *      使用 boundHashOps() 比 opsForHash() 简单
         *          public <HK, HV> BoundHashOperations<K, HK, HV> boundHashOps(K key) 指定外层的 key 获取内层的 map
         *              .values() 获取内层购物车集合
         *              .hasKey() 通过内层的 key 就可以判断当前用户是否包含这条购物车记录
         *              .put() 通过内层的 key 内层的 value 即可新增购物车记录
         *
         *  数据结构
         *      Map<userId/userKey, Map<skuId, cartJson>
         */
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);// 指定外层的 key 获取内层的 map

        // 本次新增数量 + 该数据 可能会被覆盖掉提前提取出来
        BigDecimal count = cart.getCount();

        String skuId = cart.getSkuId().toString();
        // 判断 该用户购物车是否存在该条商品 相当于 已经拿到 内层 Map<skuId, cartJson>
        if (hashOps.hasKey(skuId)) { // 此处不会空指针 因为 boundHashOps 底层直接 new DefaultBoundHashOperations 对象. 购物车可能为空 但 hashOps 不可能为空
            // 包含更新数量
            String cartJson = hashOps.get(skuId).toString(); // 获取对应的购物车记录

            // 反序列化为购物车对象
            cart = JSON.parseObject(cartJson, Cart.class); // 覆盖掉参数中的购物车对象
            cart.setCount(cart.getCount().add(count)); // 数据库中的数量累加新增的数量

            // 更新到数据库 redis
//            hashOps.put(skuId, JSON.toJSONString(cart));

            // 更新到数据库 mysql. 更新那个用户的哪条商品的购物车
            cartSyncService.updataCart(userId, cart, skuId);
            redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, cart.getPrice().toString());
        } else {
            // 不包含 新增记录, 此时购物车中只有两个参数 1. sku_id 2. count 其他参数需要调用远程接口进行设置 在保存到数据库
            cart.setUserId(userId);
            cart.setCheck(true); // 新增商品到购物车 默认设置为选中状态

            // 1. 根据 skuId 查询 sku
            ResponseVo<SkuEntity> skuEntityResponseVo = pmsClient.querySkuById(cart.getSkuId());
            // 由于是 get 请求, 用户可能会把连接放入 地址栏中进行访问, 导致我们查询的数据为空
            SkuEntity skuEntity = skuEntityResponseVo.getData();

            if (skuEntity == null) {
                throw new CartException("您要添加购物车的商品不存在!");
            }

            cart.setTitle(skuEntity.getTitle());
            cart.setPrice(skuEntity.getPrice());
            cart.setDefaultImage(skuEntity.getDefaultImage());

            // 2. 根据 skuId 查询当前 sku 的销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrsResponseVo = pmsClient.querySaleAttrValueBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrsResponseVo.getData();
            // 序列化后放入
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            // 3. 根据 skuId 查询库存
            ResponseVo<List<WareSkuEntity>> wareSkuResponseVo = wmsClient.queryWareSkuEntities(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareSkuResponseVo.getData();
            // 如果库存不为空
            if (CollectionUtils.isNotEmpty(wareSkuEntities)) {

                // 序列化后放入
                cart.setStore(
                        wareSkuEntities.stream()
                                .anyMatch(
                                        wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
                                )
                );
            }

            // 4. 根据 skuId 查询营销信息
            ResponseVo<List<ItemSaleVo>> salesResponseVo = smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            // 序列化后放入
            cart.setSales(JSON.toJSONString(itemSaleVos));

            // 保存到数据库 redis
//            hashOps.put(skuId, JSON.toJSONString(cart));

            // 保存到数据库 mysql
            cartSyncService.insertCart(userId, cart);

            redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());
        }
        // 不管是更新还是新增都会执行该方法 提取出来
        hashOps.put(skuId, JSON.toJSONString(cart));
    }


    public Cart queryCartBySkuId(Long skuId) {
        // 1. 获取登陆状态
        String userId = getUserId();

        // 获取内层 map<skuId, cartJson>
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);

        if (!hashOps.hasKey(skuId.toString())) {
            throw new CartException("您的购物车中没有该商品!");
        }

        String cartJson = hashOps.get(skuId.toString()).toString();

        return JSON.parseObject(cartJson, Cart.class);
    }

    // 不管是更新 还是 新增 还是回显 我们都会用到该方法 提取出来一个方法
    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        // 首先取 userkey, 不管 userId 是否存在 userKey 是存在的
        String userId = userInfo.getUserKey();

        // 判断 userId 是否为空
        if (userInfo.getUserId() != null) {
            // 如果 userId 不为空 使用 userId
            userId = userInfo.getUserId().toString();
        }
        return userId;
    }


    @Async
    public  void exception1() {
        try {
            System.out.println("exception1 开始执行");
            Thread.sleep(5000);
            System.out.println("exception1 执行结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        return AsyncResult.forValue("hello word1");
    }

    @Async
    public void exception2() {
        try {
            System.out.println("exception2 开始执行");
            Thread.sleep(4000);
            int i = 1/0;
            System.out.println("exception2 执行结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        return AsyncResult.forValue("hello word2");
    }

    public List<Cart> queryCarts() {
        // 1. 以userKey查询未登录购物车
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        // 获取未登录购物车
        BoundHashOperations<String, Object, Object> unloginHashOps = redisTemplate.boundHashOps(KEY_PREFIX + userKey);

        // 将未登录购物车Json字符串转换成购物车集合
        List<Object> cartJsons = unloginHashOps.values();

        List<Cart> unloginCarts = null;
        if (CollectionUtils.isNotEmpty(cartJsons)) {
            unloginCarts = cartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);

                // 设置实时价格
                String s = redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                if (StringUtils.isNotEmpty(s)) {
                    cart.setCurrentPrice(new BigDecimal(s));
                }
                return cart;
            }).collect(Collectors.toList());

        }

        // 2. 判断是否登录，如果未登录就将未登录购物车返回(userId==null)
        Long userId = userInfo.getUserId();
        if (userId == null) {
            return unloginCarts;
        }

        // 获取已登录购物车
        BoundHashOperations<String, Object, Object> loginHashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId.toString());

        // 3. 合并未登录购物车和已登录购物车
        if (CollectionUtils.isNotEmpty(unloginCarts)) {
            unloginCarts.forEach(cart -> {
                String skuId = cart.getSkuId().toString();

                if (loginHashOps.hasKey(skuId)) {
                    // 获取到登录购物车会覆盖数量，所以提前获取数量
                    BigDecimal count = cart.getCount();
                    // 已登录购物车包含此商品就修改数量
                    String cartJson = loginHashOps.get(skuId).toString();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    // mySql
                    cartSyncService.updataCart(userId.toString(), cart, skuId.toString());

                } else {
                    // 已登录购物车不包含此商品就进行新增
                    cart.setId(null);
                    cart.setUserId(userId.toString());
                    // mySql
                    cartSyncService.insertCart(userId.toString(), cart);
                }
                // redis
                loginHashOps.put(skuId, JSON.toJSONString(cart));

            });

            // 4. 删除未登录购物车
            // redis
            redisTemplate.delete(KEY_PREFIX + userKey);

            // mysql
            cartSyncService.deleteByUserId(userKey);
        }
        // 5. 返回合并后的已登录购物车
        List<Object> loginCartJsons = loginHashOps.values();
        if (CollectionUtils.isNotEmpty(loginCartJsons)) {
            List<Cart> loginCart = loginCartJsons.stream().map(loginCartJson -> {
                Cart cart = JSON.parseObject(loginCartJson.toString(), Cart.class);
                // 设置实时价格
                String s = redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                if (StringUtils.isNotEmpty(s)) {
                    cart.setCurrentPrice(new BigDecimal(s));
                }

                return cart;
            }).collect(Collectors.toList());

            return loginCart;
        }


        return null;
    }

    public void updataNum(Cart cart) {
        // 1. 获取登录状态
        String userId = getUserId();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);

        String skuId = cart.getSkuId().toString();
        if (hashOps.hasKey(skuId)) {
            // 2. 根据skuId获取购物车对象
            Object cartJson = hashOps.get(skuId);

            // 因为获取购物车对象会覆盖数据，所以提前获取数量
            BigDecimal count = cart.getCount();

            // 3. 将购物车json字符串反序列化成购物车对象
            cart = JSON.parseObject(cartJson.toString(), Cart.class);
            cart.setCount(count);

            hashOps.put(skuId, JSON.toJSONString(cart));
            cartSyncService.updataCart(userId, cart, skuId);
        }

    }

    public void deleteCart(Long skuId) {
        // 获取登陆状态
        String userId = getUserId();

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);

        // 判断该用户是否有这个商品
        if (hashOps.hasKey(skuId.toString())) {

            // 删除商品
            // redis
            hashOps.delete(skuId.toString());
            // mysql
            cartSyncService.deleteByUserIdAndSkuId(userId, skuId);
        }

    }

    public List<Cart> queryCheckedCartsByUserId(Long userId) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userId);
        List<Object> cartsJson = hashOps.values();

        // 判空，如果购物车json字符串不为空再进行遍历判断返回
        if (CollectionUtils.isNotEmpty(cartsJson)) {
            return cartsJson.stream().map(cartJson -> JSON.parseObject(cartJson.toString(), Cart.class))
                    .filter(Cart::getCheck).collect(Collectors.toList());
        }
        // 如果购物车集合为空则报错
        throw new RuntimeException("您的订单没有商品");
    }
}
