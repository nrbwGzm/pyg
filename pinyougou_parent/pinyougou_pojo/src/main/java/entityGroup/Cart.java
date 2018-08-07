package entityGroup;

import com.pinyougou.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.List;

//购物车类,一个商家的多种商品为一个购物车类
public class Cart implements Serializable {

    //商家id
    private String sellerId;
    //商家名称
    private String sellerName;

    //TbOrderItem : 订单类,一个sku商品对应一个订单,含有购买数量属性
    //List<TbOrderItem> : 订单类集合
    private List<TbOrderItem> orderItemList;

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<TbOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<TbOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
