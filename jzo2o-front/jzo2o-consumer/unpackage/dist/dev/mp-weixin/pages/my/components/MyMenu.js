"use strict";
const common_vendor = require("../../../common/vendor.js");
const _sfc_main = {
  __name: "MyMenu",
  emits: ["handleAddress", "handleBill"],
  setup(__props, { emit: __emit }) {
    const emit = __emit;
    const handleAddress = () => {
      emit("handleAddress");
    };
    const handleCoupon = () => {
      emit("handleCoupon");
    };
    const handleBill = () => {
      emit("handleBill");
    };
    return (_ctx, _cache) => {
      return {
        a: common_vendor.o(handleAddress),
        b: common_vendor.o(handleBill),
        c: common_vendor.o(handleCoupon)
      };
    };
  }
};
wx.createComponent(_sfc_main);
//# sourceMappingURL=../../../../.sourcemap/mp-weixin/pages/my/components/MyMenu.js.map
