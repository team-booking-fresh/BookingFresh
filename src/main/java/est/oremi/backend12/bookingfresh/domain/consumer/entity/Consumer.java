package est.oremi.backend12.bookingfresh.domain.consumer.entity;

import est.oremi.backend12.bookingfresh.domain.cart.Cart;
import est.oremi.backend12.bookingfresh.domain.coupon.UserCoupon;
import est.oremi.backend12.bookingfresh.domain.order.Order;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name="consumers")
public class Consumer {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "email")
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "nickname")
  private String nickname;

  @Column(name = "address")
  private String address;

  @Column(name = "detail_address")
  private String detailAddress;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  // ROLE 하드코딩
  @Column(name = "ROLE")
  private String role = "ROLE_USER";

  @OneToOne(mappedBy = "consumer", cascade = CascadeType.ALL, orphanRemoval = true)
  private Cart cart;

  @OneToMany(mappedBy = "consumer")
  private List<Order> orders = new ArrayList<>();

  @OneToMany(mappedBy = "consumer")
  private List<UserCoupon> userCoupons = new ArrayList<>();

  @Builder
  public Consumer(String email, String password, String nickname, String address, String detailAddress, LocalDateTime createdAt) {
    this.email = email;
    this.password = password;
    this.nickname = nickname;
    this.address = address;
    this.detailAddress = detailAddress;
    this.createdAt = createdAt;
  }


  // 사용자 닉네임 및 주소 정보 업데이트
  public void updateInfo(String nickname, String address, String detailAddress) {
    if (nickname != null && !nickname.trim().isEmpty()) {
      this.nickname = nickname;
    }
    if (address != null && !address.trim().isEmpty()) {
      this.address = address;
    }
    if (detailAddress != null) {
      this.detailAddress = detailAddress;
    }
  }

  // 사용자 비밀번호 업데이트
  public void updatePassword(String encodedNewPassword) {
    if (encodedNewPassword == null || encodedNewPassword.trim().isEmpty()) {
      throw new IllegalArgumentException("암호화된 새 비밀번호는 null이 될 수 없습니다.");
    }
    this.password = encodedNewPassword;
  }
}
