package com.hcf.tax.taxauthgateway;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@RunWith(SpringRunner.class)
//SpringBootTest.WebEnvironment.RANDOM_PORT    采用随机端口
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemogatewayApplicationTests {

	@LocalServerPort
	int port;
	private WebTestClient client;

	@Before
	public void setup() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
	}


	/**
	 * 匹配规则是按照url路径匹配   匹配到 path_route 的路由规则：
	 * 本质上是将localhost:port/get  转发到   http://httpbin.org/get
	 * 返回结果用Map类型接收
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void pathRouteWorks() {
		client.get().uri("/get")
				.exchange()
				.expectStatus().isOk()
				.expectBody(Map.class)
				.consumeWith(result -> {
					System.out.println(result.toString());
					assertThat(result.getResponseBody()).isNotEmpty();
				});
	}


	/**
	 *  按照请求的heard中的Host匹配     匹配到 host_route 的路由规则：
	 * 本质上是localhost:port/headers请求  转发到   http://httpbin.org/headers
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void hostRouteWorks() {
		client.get().uri("/headers")
				//自定义请求的头部Host参数
				.header("Host", "www.myhost.org")
				.exchange()
				.expectStatus().isOk()
				.expectBody(Map.class)
				.consumeWith(result -> {
					System.out.println(result.toString());
					assertThat(result.getResponseBody()).isNotEmpty();
				});
	}


	/**
	 * 根据请求的Host匹配到rewrite_route路由规则
	 * 重写请求url，去掉请求url中的foo，   localhost:port/foo/get  转为  http://httpbin.org/get
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void rewriteRouteWorks() {
		client.get().uri("/foo/get")
				.header("Host", "www.rewrite.org")
				.exchange()
				.expectStatus().isOk()
				.expectBody(Map.class)
				.consumeWith(result -> {
					assertThat(result.getResponseBody()).isNotEmpty();
				});
	}


	/**
	 * 路由超时熔断测试
	 *
	 * 请求装发到http://httpbin.org/delay/3会延时3s返回，自动触发熔断机制
	 *
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void hystrixRouteWorks() {
		client.get().uri("/delay/3")
				.header("Host", "www.hystrix.org")
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
		;
	}


	/**
	 * 请求超时熔断后，调用回调方法，降级处理
	 *
	 * 降级方法返回： This is a fallback
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void hystrixFallbackRouteWorks() {
		client.get().uri("/delay/3")
				.header("Host", "www.hystrixfallback.org")
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class).isEqualTo("This is a fallback")
				.consumeWith(result -> {
					System.out.println(result.toString());
					assertThat(result.getResponseBody()).isNotEmpty();
				});
	}


	/**
	 *
	 */
	@Test
	public void rateLimiterWorks() {
		WebTestClient authClient = client.mutate()
				.filter(basicAuthentication("user", "password"))
				.build();

		boolean wasLimited = false;

		for (int i = 0; i < 20; i++) {
			FluxExchangeResult<Map> result = authClient.get()
					.uri("/anything/1")
					.header("Host", "www.limited.org")
					//.attributes()
					.exchange()
					.returnResult(Map.class);
			System.out.println(result.toString());
			if (result.getStatus().equals(HttpStatus.TOO_MANY_REQUESTS)) {
				System.out.println("Received result: "+result);
				wasLimited = true;
				break;
			}
		}

		if(!wasLimited){
			System.out.println("请求全部正确，没有发生限流");
		}
		assertThat(wasLimited)
				.as("A HTTP 429 TOO_MANY_REQUESTS was not received")
				.isTrue();

	}



	/**
	 * 匹配规则是按照url路径匹配   匹配到 path_route 的路由规则：
	 * 本质上是将localhost:port/get  转发到   http://httpbin.org/get
	 * 返回结果用Map类型接收
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void authPathRouteWorks() {
		client.get().uri("/auth/test")
				.exchange()
				.expectStatus().isOk()
				.expectBody(Map.class)
				.consumeWith(result -> {
					System.out.println(result.toString());
					assertThat(result.getResponseBody()).isNotEmpty();
				});
	}

}
