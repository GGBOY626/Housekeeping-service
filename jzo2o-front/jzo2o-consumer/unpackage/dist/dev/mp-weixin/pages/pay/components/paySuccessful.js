"use strict";
const common_vendor = require("../../../common/vendor.js");
const common_assets = require("../../../common/assets.js");
if (!Array) {
  const _component_NavBar = common_vendor.resolveComponent("NavBar");
  _component_NavBar();
}
const _sfc_main = {
  __name: "paySuccessful",
  setup(__props) {
    const store = common_vendor.useStore();
    const handleToIndex = () => {
      common_vendor.index.navigateTo({
        url: "/pages/index/index"
      });
    };
    const handleToOrder = () => {
      store.commit("user/setOrderStatus", "");
      store.commit("user/setBackLike", "pay");
      common_vendor.index.navigateTo({
        url: "/subPages/order/index"
      });
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.p({
          title: "下单成功",
          isShowBack: true,
          handleToLink: handleToIndex
        }),
        b: common_assets._imports_0$7,
        c: common_vendor.o(handleToIndex),
        d: common_vendor.o(handleToOrder)
      };
    };
  }
};
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-02dc2ad2"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/pay/components/paySuccessful.js.map
