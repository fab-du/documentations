package de.app;
import de.security.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@SpringBootApplication
@EnableScheduling
@Controller
public class Application extends SpringBootServletInitializer{

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(Application.class);
    }

	// Match everything without a suffix (so not a static resource)
	@RequestMapping(value = "/{[path:[^\\.]*}")
	public String redirect() {
		// Forward to home page so that route is preserved.
		return "forward:/";
	}
	
	@RequestMapping(value = "/free")
	@ResponseBody
	public Map<String, Object> foo() {
		// Forward to home page so that route is preserved.
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("content", "authenticate");
		return model;
	}

	
//	@Override
//	public void onStartup(ServletContext servletContext) throws ServletException {
//		//servletContext.getSessionCookieConfig().
//		//servletContext.addFilter(filterName, filterClass)
//		servletContext.setInitParameter("filter", "");
//		super.onStartup(servletContext);
//	}
	
	@Configuration
	@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
	protected static class SecurityConfiguration extends WebSecurityConfigurerAdapter {

		public SecurityConfiguration() {
			super(false);
		}
		
		@Override
		public void configure(WebSecurity web) throws Exception {
          web.ignoring().antMatchers("/free", "/session/**", "/error");
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.httpBasic().and().authorizeRequests()
			.antMatchers("/index.html", "/", "/login", "/message", "/home", "/free", "/session/**")
			.permitAll().anyRequest().authenticated().and().csrf().disable()
			.addFilterBefore( authProcessingFilter( this.authenticationManager(), this.tokenUtils()), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint())
            .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and().anonymous();
		}

		private Filter authProcessingFilter( AuthenticationManager authManager, TokenUtils tokenUtils ) {
            return new AuthFilter( authManager, tokenUtils );
		}

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(tokenAuthenticationProvider());
        }

        @Bean
        public AuthenticationProvider tokenAuthenticationProvider() {
            return new AuthProvider();
        }
        
        @Bean 
        public TokenUtils tokenUtils(){
        	return new TokenUtils();
        }

        @Bean
        public AuthenticationEntryPoint unauthorizedEntryPoint() {
            return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

	}
	
//	@Override
//	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//		return application.sources(Application.class);
//	}	
	
	public static void main( String[] args ) {
		SpringApplication.run(Application.class, args);	
		Application.makeUploadDir();
	}
	
	public static void makeUploadDir(){
		File file = new File("uploads");
		
		if( !file.exists()){
			if( file.mkdir()){
				System.out.println("Make upload dir");
			}
			else{
				System.out.println("Upload dir already exists");
			}
		}
	}
}
