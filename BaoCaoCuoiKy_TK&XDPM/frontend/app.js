// =================================================================
// NEXUS STORE - FRONTEND SIMULATOR
// Mô phỏng 100% logic của Clean Architecture & Domain Rules từ Backend Java
// =================================================================

const $ = (id) => document.getElementById(id);
const formatMoney = (n) => new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(n);

// -------------------------------------------------------------
// DỮ LIỆU MOCK ĐỒNG BỘ VỚI InMemoryOrderDBMock & InMemoryVoucherDBMock
// -------------------------------------------------------------
const DB = {
    // Tồn kho mỹ phẩm chính hãng
    stock: {
        P1: { name: "Serum Vitamin C Dưỡng Sáng", price: 300000, stock: 10 },
        P2: { name: "Kem Chống Nắng Phổ Rộng SPF50+", price: 250000, stock: 5 },
        P3: { name: "Nước Thần Phục Hồi Da 150ml", price: 500000, stock: 3 },
        P4: { name: "Son Dưỡng Ẩm Không Màu", price: 80000, stock: 20 }
    },

    // Đơn hàng / Giỏ hàng
    orders: {
        "CART-01": { customerId: "CUS-01", status: "ACTIVE" },
        "CART-02": { customerId: "CUS-01", status: "PAID" },
        "CART-03": { customerId: "CUS-02", status: "ACTIVE" },
        "ORD-001": { customerId: "CUS-01", status: "ACTIVE" }
    },

    // Danh mục Voucher
    vouchers: {
        SALE10: {
            type: "PERCENTAGE",
            value: 10,
            maxCap: 40000,
            minSpend: 300000,
            uses: 5,
            status: "ACTIVE",
            desc: "Giảm 10%, max 40k, đơn từ 300k"
        },
        FIXED50: {
            type: "FIXED_AMOUNT",
            value: 50000,
            maxCap: null,
            minSpend: 200000,
            uses: 5,
            status: "ACTIVE",
            desc: "Giảm thẳng 50.000đ, đơn từ 200k"
        },
        BIGSALE: {
            type: "FIXED_AMOUNT",
            value: 100000,
            maxCap: null,
            minSpend: 50000,
            uses: 5,
            status: "ACTIVE",
            desc: "Giảm 100k, không làm âm tổng tiền"
        },
        FLAT100: {
            type: "FIXED_AMOUNT",
            value: 100000,
            maxCap: null,
            minSpend: 200000,
            uses: 2,
            status: "ACTIVE",
            desc: "Giảm 100.000đ, đơn từ 200k"
        },
        EXPIRED10: {
            type: "PERCENTAGE",
            value: 10,
            maxCap: 40000,
            minSpend: 100000,
            uses: 5,
            status: "EXPIRED",
            desc: "Voucher đã hết hạn sử dụng"
        },
        OUTOFSTOCK: {
            type: "FIXED_AMOUNT",
            value: 50000,
            maxCap: null,
            minSpend: 100000,
            uses: 0,
            status: "EXHAUSTED",
            desc: "Voucher đã hết số lượt dùng"
        }
    },

    // Bản ghi lịch sử voucher đã áp dụng cho đơn: "orderId#code" -> discountAmount
    appliedVouchers: new Map()
};

// -------------------------------------------------------------
// NAVIGATION TABS
// -------------------------------------------------------------
document.querySelectorAll(".nav-item").forEach(button => {
    button.addEventListener("click", () => {
        document.querySelectorAll(".nav-item").forEach(b => b.classList.remove("active"));
        document.querySelectorAll(".tab-panel").forEach(p => p.classList.remove("active"));

        button.classList.add("active");
        const tab = button.dataset.tab;
        $(`${tab}-panel`).classList.add("active");

        if (tab === "order") {
            $("header-title").innerText = "Quy Trình Đặt Hàng Mỹ Phẩm & Thanh Toán (Feature 2)";
        } else {
            $("header-title").innerText = "Kiểm Tra & Áp Dụng Mã Ưu Đãi Làm Đẹp (Feature 1)";
        }
    });
});

// -------------------------------------------------------------
// TAB 1: LOGIC ĐẶT HÀNG (PLACE ORDER SERVICE)
// -------------------------------------------------------------
let cartItems = [
    { productId: "P1", quantity: 2, unitPrice: 300000 }
];

function renderCartItems() {
    const container = $("order-items-list");
    container.innerHTML = "";

    cartItems.forEach((item, index) => {
        const row = document.createElement("div");
        row.className = "item-row";

        let options = Object.keys(DB.stock).map(id => {
            const p = DB.stock[id];
            const selected = id === item.productId ? "selected" : "";
            return `<option value="${id}" ${selected}>${id} - ${p.name} (${formatMoney(p.price)})</option>`;
        }).join("");

        row.innerHTML = `
            <select class="item-select" data-index="${index}">
                ${options}
            </select>
            <input type="number" class="item-qty" data-index="${index}" value="${item.quantity}" min="-2" max="50">
            <input type="number" class="item-price" data-index="${index}" value="${item.unitPrice}" step="10000">
            <button type="button" class="btn-del" data-index="${index}" title="Xóa dòng">✕</button>
        `;
        container.appendChild(row);
    });

    // Gắn sự kiện thay đổi
    container.querySelectorAll(".item-select").forEach(sel => {
        sel.addEventListener("change", (e) => {
            const idx = e.target.dataset.index;
            const pId = e.target.value;
            cartItems[idx].productId = pId;
            cartItems[idx].unitPrice = DB.stock[pId].price;
            renderCartItems();
            updateCalculations();
        });
    });

    container.querySelectorAll(".item-qty").forEach(inp => {
        inp.addEventListener("input", (e) => {
            const idx = e.target.dataset.index;
            cartItems[idx].quantity = parseInt(e.target.value) || 0;
            updateCalculations();
        });
    });

    container.querySelectorAll(".item-price").forEach(inp => {
        inp.addEventListener("input", (e) => {
            const idx = e.target.dataset.index;
            cartItems[idx].unitPrice = parseFloat(e.target.value) || 0;
            updateCalculations();
        });
    });

    container.querySelectorAll(".btn-del").forEach(btn => {
        btn.addEventListener("click", (e) => {
            const idx = e.target.dataset.index;
            cartItems.splice(idx, 1);
            renderCartItems();
            updateCalculations();
        });
    });

    updateCalculations();
}

$("btn-add-item").addEventListener("click", () => {
    cartItems.push({ productId: "P2", quantity: 1, unitPrice: DB.stock.P2.price });
    renderCartItems();
});

// Tính toán OrderPricingService
function updateCalculations() {
    let subtotal = 0;
    cartItems.forEach(it => {
        subtotal += it.quantity * it.unitPrice;
    });

    const declaredDiscount = parseFloat($("order-voucher-discount").value) || 0;
    // Freeship khi tạm tính >= 500k
    const shippingFee = (subtotal >= 500000) ? 0 : 30000;
    const total = Math.max(0, (subtotal - declaredDiscount) + shippingFee);

    $("sum-subtotal").innerText = formatMoney(subtotal);
    $("sum-discount").innerText = "-" + formatMoney(declaredDiscount);
    $("sum-shipping").innerText = formatMoney(shippingFee);
    $("sum-total").innerText = formatMoney(total);

    const shipBadge = $("shipping-badge");
    if (subtotal >= 500000) {
        shipBadge.innerText = "✓ Được Miễn Phí Vận Chuyển";
        shipBadge.className = "badge badge-green";
    } else {
        shipBadge.innerText = `Mua thêm ${formatMoney(500000 - subtotal)} để Freeship`;
        shipBadge.className = "badge badge-purple";
    }
}

$("order-voucher-discount").addEventListener("input", updateCalculations);

// Render kho hàng
function renderStockDisplay() {
    const box = $("stock-display");
    box.innerHTML = "";
    Object.keys(DB.stock).forEach(id => {
        const item = DB.stock[id];
        const div = document.createElement("div");
        div.className = "stock-item";
        div.innerHTML = `
            <div class="stock-item-id">${id}</div>
            <div class="stock-item-name">${item.name}</div>
            <div class="stock-item-qty">
                <span>Kho còn:</span>
                <span class="stock-val">${item.stock} cái</span>
            </div>
        `;
        box.appendChild(div);
    });
}

// Xử lý nộp form đặt hàng (PlaceOrderService.execute())
$("order-form").addEventListener("submit", (e) => {
    e.preventDefault();

    const customerId = $("order-customer-id").value.trim();
    const cartId = $("order-cart-id").value.trim();
    const receiverName = $("ship-name").value.trim();
    const phone = $("ship-phone").value.trim();
    const address = $("ship-address").value.trim();
    const paymentMethod = $("payment-method").value;
    const voucherCode = $("order-voucher-code").value.trim();
    const discountDeclared = parseFloat($("order-voucher-discount").value) || 0;

    let subtotal = 0;
    cartItems.forEach(i => subtotal += i.quantity * i.unitPrice);
    const shippingFee = (subtotal >= 500000) ? 0 : 30000;
    const totalPayable = Math.max(0, (subtotal - discountDeclared) + shippingFee);

    const resultBox = $("order-result-box");

    // BẮT ĐẦU KIỂM TRA TOÀN BỘ 10 RULES
    // Rule 1: Giỏ hàng phải có ít nhất một sản phẩm
    if (cartItems.length === 0) {
        return renderOrderResult(false, "Giỏ hàng không có sản phẩm", { subtotal, totalPayable });
    }

    // Rule 2: Chỉ chủ giỏ hàng mới được đặt hàng
    const orderData = DB.orders[cartId];
    if (!orderData || orderData.customerId !== customerId) {
        return renderOrderResult(false, "Chỉ chủ giỏ hàng mới được đặt hàng", {
            reason: `Giỏ hàng mỹ phẩm ${cartId} không thuộc về khách hàng ${customerId}.`
        });
    }

    // Rule 3: Chỉ đơn hàng đang hoạt động mới được đặt (ACTIVE)
    if (orderData.status !== "ACTIVE") {
        return renderOrderResult(false, "Chỉ đơn hàng đang hoạt động mới được đặt hàng", {
            reason: `Trạng thái hiện tại của đơn là '${orderData.status}'.`
        });
    }

    // Rule 4: Số lượng phải là số nguyên dương và không vượt quá tồn kho
    for (const item of cartItems) {
        const stockQty = DB.stock[item.productId]?.stock || 0;
        const pName = DB.stock[item.productId]?.name || item.productId;
        if (item.quantity <= 0 || item.quantity > stockQty) {
            return renderOrderResult(false, "Số lượng sản phẩm không hợp lệ hoặc vượt quá tồn kho", {
                reason: `Mỹ phẩm '${pName}' (${item.productId}): Bạn đặt ${item.quantity}, nhưng kho chỉ còn ${stockQty} sản phẩm.`
            });
        }
    }

    // Rule 5: Thông tin giao hàng phải hợp lệ
    const isValidPhone = /^0\d{9}$/.test(phone);
    if (!receiverName || !isValidPhone || !address) {
        return renderOrderResult(false, "Thông tin giao hàng không hợp lệ", {
            reason: !isValidPhone ? "Số điện thoại phải có 10 chữ số và bắt đầu bằng số 0." : "Tên hoặc địa chỉ người nhận không được để trống."
        });
    }

    // Rule 8: Kiểm tra voucher & số tiền giảm
    if (voucherCode) {
        const key = `${cartId}#${voucherCode}`;
        if (!DB.appliedVouchers.has(key)) {
            return renderOrderResult(false, "Voucher chưa được áp dụng cho đơn hàng này", {
                reason: `Mã '${voucherCode}' chưa được ghi nhận qua Use Case ApplyVoucher cho đơn ${cartId}.`
            });
        }
    }

    if (discountDeclared < 0 || discountDeclared > subtotal) {
        return renderOrderResult(false, "Số tiền giảm giá không hợp lệ", {
            reason: `Số tiền giảm (${formatMoney(discountDeclared)}) không được âm hoặc vượt quá tạm tính (${formatMoney(subtotal)}).`
        });
    }

    // Rule 6: Thanh toán COD chỉ áp dụng cho đơn tối đa 20.000.000 VND
    if (paymentMethod === "COD" && totalPayable > 20000000) {
        return renderOrderResult(false, "Thanh toán COD chỉ áp dụng cho đơn tối đa 20000000 VND", {
            reason: `Tổng đơn là ${formatMoney(totalPayable)} vượt quá hạn mức 20.000.000đ của COD.`
        });
    }

    // THÀNH CÔNG (Rule 9): Lưu đơn, chuyển PAID, trừ tồn kho, phát Domain Event
    cartItems.forEach(item => {
        DB.stock[item.productId].stock -= item.quantity;
    });
    orderData.status = "PAID";

    renderStockDisplay();
    renderOrderResult(true, "Đặt hàng thành công", {
        cartId,
        subtotal,
        discountDeclared,
        shippingFee,
        totalPayable,
        paymentMethod,
        event: `OrderPlacedEvent(orderId='${cartId}', totalPayable=${totalPayable})`
    });
});

function renderOrderResult(success, message, data = {}) {
    const box = $("order-result-box");
    box.innerHTML = `
        <div class="response-box ${success ? 'success' : 'error'}">
            <div class="response-header">
                <span style="font-size: 20px;">${success ? '✅' : '❌'}</span>
                <span class="response-title">${message}</span>
            </div>
            <div class="response-details">
                ${data.reason ? `<div><b>Chi tiết:</b> ${data.reason}</div>` : ''}
                ${data.cartId ? `<div><b>Mã đơn:</b> <code>${data.cartId}</code> | <b>Trạng thái:</b> <code>PAID</code></div>` : ''}
                ${data.totalPayable !== undefined ? `<div><b>Tổng thanh toán:</b> <code style="color: #6ee7b7">${formatMoney(data.totalPayable)}</code></div>` : ''}
                ${data.event ? `<div><b>Domain Event:</b> <code>${data.event}</code></div>` : ''}
            </div>
        </div>
    `;
}

$("btn-reset-order").addEventListener("click", () => {
    DB.stock.P1.stock = 10;
    DB.stock.P2.stock = 5;
    DB.stock.P3.stock = 3;
    DB.stock.P4.stock = 20;
    DB.orders["CART-01"].status = "ACTIVE";
    renderStockDisplay();
    renderCartItems();
    $("order-result-box").innerHTML = `
        <div class="empty-state">
            <span class="empty-icon">🛒</span>
            <p>Dữ liệu mẫu đã được làm mới.</p>
        </div>
    `;
});

// -------------------------------------------------------------
// TAB 2: LOGIC ÁP DỤNG VOUCHER (APPLY VOUCHER SERVICE)
// -------------------------------------------------------------
function renderVoucherTable() {
    const tbody = $("voucher-table-body");
    tbody.innerHTML = "";

    Object.keys(DB.vouchers).forEach(code => {
        const v = DB.vouchers[code];
        const tr = document.createElement("tr");

        let statusClass = "status-active";
        if (v.status === "EXPIRED") statusClass = "status-expired";
        if (v.status === "EXHAUSTED") statusClass = "status-exhausted";

        tr.innerHTML = `
            <td><strong>${code}</strong></td>
            <td>${v.type === "PERCENTAGE" ? "Phần trăm" : "Cố định"}</td>
            <td>${v.type === "PERCENTAGE" ? v.value + "%" : formatMoney(v.value)}</td>
            <td>${v.maxCap ? formatMoney(v.maxCap) : "Không"}</td>
            <td>${formatMoney(v.minSpend)}</td>
            <td><b>${v.uses}</b></td>
            <td class="${statusClass}">${v.status}</td>
        `;
        tbody.appendChild(tr);
    });

    // Render quick buttons
    const tagList = $("quick-voucher-list");
    tagList.innerHTML = "";
    Object.keys(DB.vouchers).forEach(code => {
        const chip = document.createElement("button");
        chip.type = "button";
        chip.className = "voucher-chip";
        chip.innerText = code;
        chip.addEventListener("click", () => {
            $("voucher-input-code").value = code;
        });
        tagList.appendChild(chip);
    });
}

$("voucher-form").addEventListener("submit", (e) => {
    e.preventDefault();

    const orderId = $("voucher-order-id").value.trim();
    const orderAmount = parseFloat($("voucher-order-amount").value) || 0;
    const code = $("voucher-input-code").value.trim().toUpperCase();
    const orderStatus = $("voucher-order-status").value;

    const resultBox = $("voucher-result-box");

    // Rule 6: Đơn hàng phải ở trạng thái ACTIVE
    if (orderStatus !== "ACTIVE") {
        return renderVoucherResult(false, "Hệ thống từ chối áp dụng voucher", {
            reason: `Đơn hàng đang ở trạng thái '${orderStatus}' (Chỉ đơn ACTIVE mới được áp dụng).`
        });
    }

    // Rule 8: Không được áp dụng lặp lại cho cùng 1 đơn hàng
    const key = `${orderId}#${code}`;
    if (DB.appliedVouchers.has(key)) {
        return renderVoucherResult(false, "Hệ thống từ chối yêu cầu áp dụng voucher", {
            reason: `Voucher '${code}' đã được áp dụng trước đó cho đơn hàng '${orderId}'.`
        });
    }

    // Tìm voucher
    const voucher = DB.vouchers[code];
    if (!voucher) {
        return renderVoucherResult(false, "Hệ thống từ chối áp dụng voucher", {
            reason: `Mã giảm giá '${code}' không tồn tại trên hệ thống.`
        });
    }

    // Rule 5: Voucher phải ACTIVE và còn lượt
    if (voucher.status !== "ACTIVE" || voucher.uses <= 0) {
        return renderVoucherResult(false, "Hệ thống từ chối áp dụng voucher", {
            reason: voucher.status === "EXPIRED" ? "Mã giảm giá đã hết hạn sử dụng." : "Mã giảm giá đã hết lượt sử dụng."
        });
    }

    // Rule 3: Đơn hàng phải đạt chi tiêu tối thiểu
    if (orderAmount < voucher.minSpend) {
        return renderVoucherResult(false, "Hệ thống từ chối áp dụng voucher", {
            reason: `Đơn hàng cần đạt tối thiểu ${formatMoney(voucher.minSpend)} (hiện tại: ${formatMoney(orderAmount)}).`
        });
    }

    // Rule 1, 2, 4, 7: Tính toán giảm giá (VoucherCalculationService)
    let discount = 0;
    if (voucher.type === "PERCENTAGE") {
        discount = (orderAmount * voucher.value) / 100.0;
        if (voucher.maxCap && discount > voucher.maxCap) {
            discount = voucher.maxCap;
        }
    } else {
        discount = voucher.value;
    }

    // Mức giảm không được vượt quá giá trị đơn hàng
    if (discount > orderAmount) {
        discount = orderAmount;
    }

    const payable = orderAmount - discount;

    // Giảm 1 lượt dùng
    voucher.uses -= 1;
    if (voucher.uses === 0) {
        voucher.status = "EXHAUSTED";
    }

    // Ghi nhận đã áp dụng
    DB.appliedVouchers.set(key, discount);

    // Đồng bộ sang form đặt hàng để tiện liên thông
    $("order-cart-id").value = orderId;
    $("order-voucher-code").value = code;
    $("order-voucher-discount").value = discount;
    updateCalculations();
    renderVoucherTable();

    renderVoucherResult(true, "Áp dụng voucher thành công!", {
        code,
        discount,
        payable,
        remainingUses: voucher.uses,
        event: `VoucherAppliedEvent(orderId='${orderId}', code='${code}', discount=${discount})`
    });
});

function renderVoucherResult(success, message, data = {}) {
    const box = $("voucher-result-box");
    box.innerHTML = `
        <div class="response-box ${success ? 'success' : 'error'}">
            <div class="response-header">
                <span style="font-size: 20px;">${success ? '🎉' : '⚠️'}</span>
                <span class="response-title">${message}</span>
            </div>
            <div class="response-details">
                ${data.reason ? `<div><b>Lý do:</b> ${data.reason}</div>` : ''}
                ${data.discount !== undefined ? `<div><b>Số tiền được giảm:</b> <code style="color:#34d399">-${formatMoney(data.discount)}</code></div>` : ''}
                ${data.payable !== undefined ? `<div><b>Số tiền phải thanh toán:</b> <code>${formatMoney(data.payable)}</code></div>` : ''}
                ${data.remainingUses !== undefined ? `<div><b>Lượt sử dụng còn lại:</b> <code>${data.remainingUses}</code></div>` : ''}
                ${data.event ? `<div><b>Domain Event:</b> <code>${data.event}</code></div>` : ''}
            </div>
        </div>
    `;
}

// Khởi chạy ban đầu
renderCartItems();
renderStockDisplay();
renderVoucherTable();
