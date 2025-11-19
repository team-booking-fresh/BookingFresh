package est.oremi.backend12.bookingfresh.domain.consumer.controller;

import est.oremi.backend12.bookingfresh.domain.consumer.dto.AddConsumerRequest;
import est.oremi.backend12.bookingfresh.domain.consumer.dto.LoginRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping
public class AuthenticationPageController {

    // 리다이랙트 컨트롤러
    @GetMapping("/home")
    public String homeRedirect() {
        return "redirect:/products";
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/products";
    }

    @GetMapping("/signup")
    public String signUpPage(Model model, HttpServletRequest request) {
        model.addAttribute("consumerRequest", new AddConsumerRequest());
        model.addAttribute("isLoggedIn", false);

        return "authentication/signup";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        model.addAttribute("loginRequest", new LoginRequest());
        model.addAttribute("isLoggedIn", false);

        return "authentication/login";
    }

    // 마이페이지 (/mypage)
    @GetMapping("/mypage")
    public String myPage(HttpServletRequest request, Model model) {
        boolean loggedIn = isLoggedIn(request);
        if (!loggedIn) {
            return "redirect:/login";
        }
        // 로그인 되어있다면, 모델에 상태를 담고 페이지 렌더링
        model.addAttribute("isLoggedIn", true);
        return "mypage/mypage";
    }

    @GetMapping("/mypage/edit")
    public String mypageEdit(HttpServletRequest request, Model model) {

        boolean loggedIn = isLoggedIn(request);
        if (!loggedIn) {
            return "redirect:/login";
        }

        model.addAttribute("isLoggedIn", true);
        return "mypage/edit";
    }

    @GetMapping("/mypage/coupons")
    public String mypageCoupons(HttpServletRequest request, Model model) {

        boolean loggedIn = isLoggedIn(request);
        if (!loggedIn) {
            return "redirect:/login";
        }

        model.addAttribute("isLoggedIn", true);
        return "mypage/coupons";
    }

    @GetMapping("/mypage/wishlist")
    public String mypageWishlist(HttpServletRequest request, Model model) {

        boolean loggedIn = isLoggedIn(request);
        if (!loggedIn) {
            return "redirect:/login";
        }

        model.addAttribute("isLoggedIn", true);
        return "mypage/wishlist";
    }

    // 주문 상세 페이지
    @GetMapping("/mypage/orders/{orderId}")
    public String orderDetailPage(
            @PathVariable Long orderId,
            HttpServletRequest request,
            Model model) {

        // 비로그인 시 리다이렉트
        boolean loggedIn = isLoggedIn(request);
        if (!loggedIn) {
            return "redirect:/login";
        }

        //  모델에 로그인 상태와 주문 ID 추가
        model.addAttribute("isLoggedIn", true);
        model.addAttribute("orderId", orderId);


        return "mypage/order-detail";
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

/*    @GetMapping("/mypage/wishlist")
    public String mypageWishlist() {
        return "mypage/wishlist";
        // 이렇게 구현해보려 했는데, 결론적으로는 웹 스토리지에 저장하는
        // AT를 어떻게 바로 불러올 방법이 없어서 조건부 랜더링을 구현하려면 더 많은 코드가 필요해서 일단 익순한 방법으로 진행
    }*/

}