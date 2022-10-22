package me.chulgil.spring.meeting.modules.main;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) // 런터임 에도 유지
@Target(ElementType.PARAMETER)
// 현재 이 애노테이션을 참조하고 있는 객체가 아래 문자열이면 null로 파라미터를 세팅하고 아니면 프로퍼티의 어카운트를 꺼내서 설정
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")
public @interface CurrentUser {
}
