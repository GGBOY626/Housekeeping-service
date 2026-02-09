"use strict";
const common_vendor = require("../../common/vendor.js");
if (!Array) {
  const _easycom_uni_popup2 = common_vendor.resolveComponent("uni-popup");
  _easycom_uni_popup2();
}
const _easycom_uni_popup = () => "../../uni_modules/uni-popup/components/uni-popup/uni-popup.js";
if (!Math) {
  _easycom_uni_popup();
}
const _sfc_main = {
  __name: "index",
  props: {
    // 选择的时间
    errorTipText: {
      type: Object,
      default: () => ({})
    },
    title: {
      type: String,
      default: ""
    }
  },
  emits: ["@handleSub"],
  setup(__props, { expose: __expose, emit: __emit }) {
    const popup = common_vendor.ref(null);
    const emit = __emit;
    const handleClose = () => {
      popup.value.close();
    };
    const handleSub = () => {
      emit("handleSub");
    };
    __expose({
      popup
    });
    return (_ctx, _cache) => {
      return {
        a: common_vendor.t(__props.title),
        b: common_vendor.t(__props.errorTipText.text),
        c: common_vendor.o(handleClose),
        d: common_vendor.o(handleSub),
        e: _ctx.type === "left" || _ctx.type === "right" ? 1 : "",
        f: common_vendor.sr(popup, "e23ecdf6-0", {
          "k": "popup"
        }),
        g: common_vendor.p({
          ["mask-background-color"]: "rgba(0,0,0,0.6)"
        })
      };
    };
  }
};
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__scopeId", "data-v-e23ecdf6"]]);
wx.createComponent(Component);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/components/Operate/index.js.map
