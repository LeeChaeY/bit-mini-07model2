package com.model2.mvc.web.products;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.product.ProductService;


//==> 회원관리 Controller
@Controller
@RequestMapping("/product/*")
public class ProductController {
	
	///Field
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	//setter Method 구현 않음
		
	public ProductController(){
		System.out.println(this.getClass());
	}
	
	//==> classpath:config/common.properties  ,  classpath:config/commonservice.xml 참조 할것
	//==> 아래의 두개를 주석을 풀어 의미를 확인 할것
//	@Value("#{commonProperties['pageUnit']}")
	@Value("#{commonProperties['pageUnit'] ?: 3}")
	int pageUnit;
	
//	@Value("#{commonProperties['pageSize']}")
	@Value("#{commonProperties['pageSize'] ?: 2}")
	int pageSize;
	
	
	@RequestMapping(value="addProduct", method=RequestMethod.GET )
	public String addProductView() throws Exception {

		System.out.println("/product/addProduct : GET");
		
		return "redirect:/product/addProductView.jsp";
	}
	
	@RequestMapping(value="addProduct", method=RequestMethod.POST)
	public String addProduct( @ModelAttribute("product") Product product , Model model ) throws Exception {
		product.setManuDate(product.getManuDate().replace("-", ""));
		
		System.out.println("addProduct : POST : "+product);
		//Business Logic
		productService.addProduct(product);
		
		model.addAttribute("product", product);
		
		return "forward:/product/addProduct.jsp";
	}
	
	@RequestMapping(value="getProduct", method=RequestMethod.GET )
	public String getProduct(HttpServletRequest request, HttpServletResponse response,  
									@RequestParam("prodNo") int prodNo, @RequestParam("menu") String menu , Model model ) throws Exception {
		String resultPage = "forward:/product/getProduct.jsp";
		System.out.println("getProduct : GET : prodNo : "+prodNo+", menu : "+menu);
		//Business Logic
		Product product = productService.getProduct(prodNo);
		// Model 과 View 연결
		model.addAttribute("product", product);
		model.addAttribute("menu", menu);
		
		if (menu.equals("manage") && product.getProTranCode() == null) 
			resultPage = "redirect:/product/updateProduct?prodNo="+prodNo;

		Cookie[] cookies = request.getCookies();
		String history = null;
		if (cookies!=null && cookies.length > 0) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookie.getName().equals("history")) {
					history = cookie.getValue();
				}
			}
			if (history != null) {
				List<String> h = new ArrayList<String>(Arrays.asList(history.split(URLEncoder.encode(","))));
				if (h.indexOf(prodNo+"") != -1) h.remove(h.indexOf(prodNo+""));
				h.add(""+prodNo);
				history = String.join(URLEncoder.encode(","), h);
			}
			else {
				history = prodNo+"";
			}
		}
		
		System.out.println("getProduct :: cookie history : "+history);
		Cookie cookie = new Cookie("history", history);
		cookie.setMaxAge(60*60);
		response.addCookie(cookie);
		
		return resultPage;
	}
	
	@RequestMapping(value="updateProduct", method=RequestMethod.GET )
	public String updateProductView( @RequestParam("prodNo") int prodNo , Model model ) throws Exception{

		System.out.println("updateProduct : GET : "+prodNo);
		//Business Logic
		Product product = productService.getProduct(prodNo);
		// Model 과 View 연결
		model.addAttribute("product", product);
		
		return "forward:/product/updateProductView.jsp";
	}
	
	@RequestMapping(value="updateProduct", method=RequestMethod.POST)
	public String updateProduct( @ModelAttribute("product") Product product , Model model , HttpSession session) throws Exception{
		String menu = "search";
		
		System.out.println("updateProduct : POST : "+product);
		//Business Logic
		productService.updateProduct(product);
		
		return "redirect:/product/getProduct?prodNo="+product.getProdNo()+"&menu="+menu;
	}
	
	@RequestMapping(value="listProduct")
	public String listProduct( @ModelAttribute("search") Search search, @RequestParam("menu") String menu , 
											Model model , HttpServletRequest request) throws Exception{
		String beginPrice = null;
		String endPrice = null;
		
		if(request.getParameter("beginPrice") != null && !request.getParameter("beginPrice").equals("") 
				&& request.getParameter("endPrice") != null &&!request.getParameter("endPrice").equals("")) {
			search.setSearchCondition("2");
			beginPrice = request.getParameter("beginPrice");
			model.addAttribute("beginPrice", beginPrice);
			endPrice = request.getParameter("endPrice");
			model.addAttribute("endPrice", endPrice);
		} else if (search.getSearchKeyword() != null && !search.getSearchKeyword().equals("")) {
			search.setSearchCondition("1");
		}
		System.out.println("listProduct : GET / POST : "+search);
		System.out.println("listProduct : beginPrice : "+beginPrice+", endPrice : "+endPrice);
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		String searchKeyword = null;
		if(search.getSearchKeyword() != null && !search.getSearchKeyword().equals(""))
			searchKeyword = search.getSearchKeyword();
		
		// Business logic 수행
		Map<String , Object> map=productService.getProductList(search);
		
		if(search.getSearchKeyword() != null && !search.getSearchKeyword().equals(""))
			search.setSearchKeyword(searchKeyword);
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println("listProduct ::"+resultPage);
		
		// Model 과 View 연결
		model.addAttribute("list", map.get("list"));
		model.addAttribute("resultPage", resultPage);
		model.addAttribute("search", search);
		model.addAttribute("menu", menu);
		
		return "forward:/product/listProduct.jsp";
	}
}