package com.model2.mvc.web.purchase;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.model2.mvc.common.Page;
import com.model2.mvc.common.Search;
import com.model2.mvc.service.domain.Product;
import com.model2.mvc.service.domain.Purchase;
import com.model2.mvc.service.domain.User;
import com.model2.mvc.service.product.ProductService;
import com.model2.mvc.service.purchase.PurchaseService;


//==> 회원관리 Controller
@Controller
@RequestMapping("/purchase/*")
public class PurchaseController {
	
	///Field
	@Autowired
	@Qualifier("purchaseServiceImpl")
	private PurchaseService purchaseService;
	@Autowired
	@Qualifier("productServiceImpl")
	private ProductService productService;
	//setter Method 구현 않음
		
	public PurchaseController(){
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
	
	
	@RequestMapping(value="addPurchase", method=RequestMethod.GET )
	public ModelAndView addPurchaseView(@RequestParam("prodNo") int prodNo, HttpSession session) throws Exception {
		Purchase purchase = new Purchase();
		purchase.setBuyer((User)session.getAttribute("user"));
		purchase.setPurchaseProd(productService.getProduct(prodNo));
		System.out.println("/purchase/addPurchase : GET : "+purchase);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("forward:/purchase/addPurchaseView.jsp");
		modelAndView.addObject("purchase", purchase);
		
		return modelAndView;
	}
	
	@RequestMapping(value="addPurchase", method=RequestMethod.POST )
	public ModelAndView addPurchase( @ModelAttribute("purchase") Purchase purchase, HttpSession session) throws Exception {
		
		System.out.println("/purchase/addPurchase : POST : "+purchase);
		//Business Logic
		purchaseService.addPurchase(purchase);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("forward:/purchase/addPurchase.jsp");
		modelAndView.addObject("purchase", purchase);
		
		return modelAndView;
	}
	
	@RequestMapping(value="getPurchase", method=RequestMethod.GET )
	public ModelAndView getPurchase(@RequestParam("tranNo") int tranNo) throws Exception {
		//Business Logic
		Purchase purchase = purchaseService.getPurchase(tranNo);
		System.out.println("/purchase/getPurchase : GET : "+purchase);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("forward:/purchase/getPurchase.jsp");
		modelAndView.addObject("purchase", purchase);
		
		return modelAndView;
	}
	
	@RequestMapping(value="updatePurchase", method=RequestMethod.GET )
	public ModelAndView updatePurchaseView(@RequestParam("tranNo") int tranNo) throws Exception{
		//Business Logic
		Purchase purchase = purchaseService.getPurchase(tranNo);
		System.out.println("/purchase/updatePurchase : GET : "+purchase);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("forward:/purchase/updatePurchaseView.jsp");
		modelAndView.addObject("purchase", purchase);
		
		return modelAndView;
	}
	
	@RequestMapping(value="updatePurchase", method=RequestMethod.POST )
	public ModelAndView updatePurchase( @ModelAttribute("purchase") Purchase purchase , HttpSession session) throws Exception{
		//Business Logic
		purchaseService.updatePurchase(purchase);
		purchase = purchaseService.getPurchase(purchase.getTranNo());
		
		System.out.println("/purchase/updatePurchase : POST : "+purchase);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("forward:/purchase/getPurchase.jsp");
		modelAndView.addObject("purchase", purchase);
		modelAndView.addObject("tranNo", purchase.getTranNo());
		
		return modelAndView;
	}
	
	@RequestMapping(value="listPurchase")
	public ModelAndView listPurchase(@ModelAttribute("search") Search search, HttpServletRequest request, HttpSession session) throws Exception{
		String beginDate = null;
		String endDate = null;
		ModelAndView modelAndView = new ModelAndView();
		
		if(request.getParameter("beginDate") != null && !request.getParameter("beginDate").equals("") 
				&& request.getParameter("endDate") != null) {
			beginDate = request.getParameter("beginDate");
			endDate = request.getParameter("endDate");
			
			modelAndView.addObject("beginDate", beginDate);
			search.setSearchCondition("2");
			
			if(endDate.equals("")) {
				endDate = "SYSDATE";
			}
			modelAndView.addObject("endDate", endDate);
			search.setSearchKeyword(beginDate+","+endDate);
		} 
		System.out.println("/purchase/listPurchase : GET / POST : "+search);
		System.out.println("/purchase/listPurchase : beginDate"+beginDate+"endDate"+endDate);
		
		if(search.getCurrentPage() ==0 ){
			search.setCurrentPage(1);
		}
		search.setPageSize(pageSize);
		
		String searchKeyword = null;
		if(search.getSearchKeyword() != null && !search.getSearchKeyword().equals(""))
			searchKeyword = search.getSearchKeyword();
		
		// Business logic 수행
		Map<String , Object> map=purchaseService.getPurchaseList(search, ((User)session.getAttribute("user")).getUserId());
		
		if(search.getSearchKeyword() != null && !search.getSearchKeyword().equals(""))
			search.setSearchKeyword(searchKeyword);
		
		Page resultPage = new Page( search.getCurrentPage(), ((Integer)map.get("totalCount")).intValue(), pageUnit, pageSize);
		System.out.println("/purchase/listPurchase ::"+resultPage);
		
		modelAndView.setViewName("forward:/purchase/listPurchase.jsp");
		modelAndView.addObject("list", map.get("list"));
		modelAndView.addObject("resultPage", resultPage);
		modelAndView.addObject("search", search);
		
		return modelAndView;
	}
	
	@RequestMapping(value="updateTranCode", method=RequestMethod.GET )
	public ModelAndView updateTranCode(@RequestParam("tranNo") int tranNo, @RequestParam("currentPage") int currentPage, 
																@RequestParam("tranCode") String tranCode) throws Exception{
		Purchase purchase = new Purchase();
		purchase.setTranNo(tranNo);
		purchase.setTranCode(tranCode);
		System.out.println("/purchase/updateTranCode : GET : tranNo : "+tranNo+", tranCode : "+tranCode);
		
		//Business Logic
		purchaseService.updateTranCode(purchase);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/purchase/listPurchase?currentPage="+currentPage);
		
		return modelAndView;
	}
	
	@RequestMapping(value="updateTranCodeByProd", method=RequestMethod.GET )
	public ModelAndView updateTranCodeByProd(@RequestParam("prodNo") int prodNo, @RequestParam("currentPage") int currentPage, 
																		@RequestParam("tranCode") String tranCode) throws Exception{
		Purchase purchase = new Purchase();
		purchase.setPurchaseProd(new Product(prodNo));
		purchase.setTranCode(tranCode);
		System.out.println("/purchase/updateTranCodeByProd : GET : prodNo : "+prodNo+", tranCode : "+tranCode);
		
		//Business Logic
		purchaseService.updateTranCode(purchase);
		
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("redirect:/product/listProduct?menu=manage&currentPage="+currentPage);
		
		return modelAndView;
	}
}