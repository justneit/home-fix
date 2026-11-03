/**
 * Vietnamese UI strings (default locale). Components read strings from this dictionary instead
 * of hard-coding them, so another locale can be added by providing an object of the same shape.
 */
export const vi = {
  app: {
    name: 'HomeFix',
    tagline: 'Thợ sửa chữa uy tín, đến tận nhà',
  },
  common: {
    loading: 'Đang tải…',
    retry: 'Thử lại',
    backHome: 'Về trang chủ',
    comingSoon: 'Sắp ra mắt',
    optional: 'không bắt buộc',
  },
  nav: {
    login: 'Đăng nhập',
    register: 'Đăng ký',
    logout: 'Đăng xuất',
    dashboard: 'Tổng quan',
    openMenu: 'Mở menu tài khoản',
  },
  roles: {
    CUSTOMER: 'Khách hàng',
    WORKER: 'Thợ sửa chữa',
    ADMIN: 'Quản trị viên',
  },
  landing: {
    heroTitle: 'Sửa chữa nhà cửa nhanh chóng, minh bạch',
    heroSubtitle:
      'Mô tả sự cố, nhận báo giá từ thợ phù hợp gần bạn và theo dõi công việc đến khi hoàn thành.',
    ctaCustomer: 'Tìm thợ ngay',
    ctaWorker: 'Trở thành thợ đối tác',
    categoriesTitle: 'Dịch vụ sửa chữa',
    categoriesEmpty: 'Hiện chưa có danh mục dịch vụ nào.',
    categoriesError: 'Không thể tải danh mục dịch vụ.',
    howItWorksTitle: 'Cách HomeFix hoạt động',
    steps: [
      { title: 'Mô tả sự cố', body: 'Chọn dịch vụ, mô tả vấn đề và thời gian mong muốn.' },
      { title: 'Nhận báo giá', body: 'Thợ phù hợp gần bạn gửi báo giá để bạn so sánh.' },
      { title: 'Hoàn thành & đánh giá', body: 'Xác nhận hoàn thành và đánh giá chất lượng thợ.' },
    ],
  },
  auth: {
    email: 'Email',
    password: 'Mật khẩu',
    confirmPassword: 'Xác nhận mật khẩu',
    fullName: 'Họ và tên',
    phone: 'Số điện thoại',
    login: {
      title: 'Đăng nhập',
      description: 'Chào mừng bạn quay lại HomeFix.',
      submit: 'Đăng nhập',
      noAccount: 'Chưa có tài khoản?',
      success: 'Đăng nhập thành công',
    },
    register: {
      title: 'Tạo tài khoản',
      description: 'Bạn muốn sử dụng HomeFix với vai trò nào?',
      submit: 'Đăng ký',
      hasAccount: 'Đã có tài khoản?',
      success: 'Đăng ký thành công',
      accountTypeLabel: 'Loại tài khoản',
      customer: 'Tôi cần thuê thợ',
      customerHint: 'Đăng yêu cầu sửa chữa và chọn thợ phù hợp.',
      worker: 'Tôi là thợ sửa chữa',
      workerHint: 'Nhận việc gần bạn. Hồ sơ cần được xác minh.',
      passwordHint: 'Ít nhất 8 ký tự, gồm chữ và số.',
    },
    sessionExpired: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
  },
  validation: {
    required: 'Vui lòng nhập thông tin này',
    invalid: 'Giá trị không hợp lệ',
    email: 'Email không hợp lệ',
    passwordMin: 'Mật khẩu phải có ít nhất 8 ký tự',
    passwordMax: 'Mật khẩu quá dài',
    passwordComplexity: 'Mật khẩu phải chứa cả chữ và số',
    passwordMismatch: 'Mật khẩu xác nhận không khớp',
    phone: 'Số điện thoại không hợp lệ (VD: 0912345678)',
    tooLong: 'Nội dung quá dài',
  },
  errors: {
    VALIDATION_FAILED: 'Dữ liệu chưa hợp lệ, vui lòng kiểm tra lại.',
    MALFORMED_REQUEST: 'Yêu cầu không hợp lệ.',
    UNAUTHORIZED: 'Vui lòng đăng nhập để tiếp tục.',
    INVALID_CREDENTIALS: 'Email hoặc mật khẩu không đúng.',
    INVALID_REFRESH_TOKEN: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
    ACCESS_DENIED: 'Bạn không có quyền thực hiện thao tác này.',
    ACCOUNT_INACTIVE: 'Tài khoản của bạn đang bị khóa hoặc chưa được kích hoạt.',
    NOT_FOUND: 'Không tìm thấy dữ liệu.',
    EMAIL_ALREADY_REGISTERED: 'Email này đã được đăng ký.',
    CONFLICT: 'Dữ liệu đã thay đổi, vui lòng tải lại và thử lại.',
    RATE_LIMITED: 'Bạn thao tác quá nhanh. Vui lòng thử lại sau ít phút.',
    INTERNAL_ERROR: 'Đã có lỗi xảy ra. Vui lòng thử lại sau.',
    NETWORK: 'Không thể kết nối tới máy chủ. Vui lòng kiểm tra kết nối mạng.',
    UNKNOWN: 'Đã có lỗi xảy ra. Vui lòng thử lại.',
  },
  routeError: {
    title: 'Đã có lỗi xảy ra',
    description: 'Trang này gặp sự cố ngoài ý muốn. Vui lòng thử tải lại.',
    notFoundTitle: 'Không tìm thấy trang',
    notFoundDescription: 'Trang bạn tìm không tồn tại hoặc đã được di chuyển.',
  },
  portal: {
    greeting: 'Xin chào, {name}',
    plannedTitle: 'Tính năng đang được phát triển',
    plannedDescription: 'Các chức năng dưới đây sẽ được bổ sung trong các giai đoạn tiếp theo.',
    accountTitle: 'Thông tin tài khoản',
    customer: {
      subtitle: 'Quản lý yêu cầu sửa chữa và lịch hẹn của bạn.',
      planned: [
        'Tạo yêu cầu sửa chữa kèm hình ảnh và vị trí',
        'Xem thợ được đề xuất và báo giá',
        'Theo dõi lịch hẹn, trò chuyện với thợ',
        'Đánh giá và khiếu nại sau khi hoàn thành',
      ],
    },
    worker: {
      subtitle: 'Quản lý hồ sơ nghề nghiệp và công việc của bạn.',
      planned: [
        'Hồ sơ nghề nghiệp, kỹ năng và khu vực làm việc',
        'Lịch làm việc hàng tuần',
        'Xem yêu cầu phù hợp và gửi báo giá',
        'Quản lý lịch hẹn và cập nhật tiến độ',
      ],
    },
    admin: {
      subtitle: 'Giám sát hoạt động của nền tảng.',
      planned: [
        'Thống kê tổng quan',
        'Quản lý và xác minh thợ',
        'Quản lý danh mục dịch vụ',
        'Xử lý khiếu nại và kiểm duyệt đánh giá',
      ],
    },
  },
} as const

export type Messages = typeof vi
