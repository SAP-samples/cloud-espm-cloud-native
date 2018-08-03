package com.sap.refapps.espm.controller;

import java.math.BigDecimal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.sap.refapps.espm.model.Tax;
import com.sap.refapps.espm.model.TaxSlab;
import com.sap.refapps.espm.service.TaxService;

/**
 * This is the tax service controller
 * class which handles all the endpoints.
 *
 */
@RestController
@RequestMapping({"","/tax.svc"})
public class TaxController {

	protected static final String V1_PATH = "/api/v1";

	@Autowired
	private TaxService taxService;
	
	/**
	 * This is the root/home service.
	 * 
	 * @return string 
	 */
	@GetMapping("/")
	public String home(){
		return "Welcome to Tax Service";
	}

	/**
	 * Returns the tax based on amount provided.
	 * 
	 * @param amount
	 * @return Tax object
	 */
	@GetMapping(V1_PATH+"/calculate/tax")
	@ResponseBody
	public ResponseEntity<Tax> calculateTaxFromAmount(@RequestParam("amount") final BigDecimal amount){
			
			Tax tax = taxService.calculateTaxFromAmount(amount);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

			return new ResponseEntity<Tax>(tax,headers,HttpStatus.OK);
	}
	
	/**
	 * It is used to update the tax percentage.
	 * 
	 * @param taxSlab
	 * @return string message
	 */
	@PostMapping(V1_PATH+"/tax/update")
	@ResponseBody
	public String setTaxSlabs(@RequestBody final TaxSlab taxSlab){
		String msg = ""; 
		if(taxSlab.SLAB1 != -1 || taxSlab.SLAB2 != -1 || taxSlab.SLAB3 != -1 || taxSlab.SLAB4 != -1){
			//update the tax slabs
			taxService.updateTaxSlabs(taxSlab.SLAB1, taxSlab.SLAB2, taxSlab.SLAB3, taxSlab.SLAB4);
			msg ="Tax updated successfully..";
		}else{
			msg = "Parameter missing in the request body.";
		}
		return msg;
	}
	
	
	/**
	 * It handles all error message.
	 * 
	 * @param message
	 * @param status
	 * @return ResponseEtity with headers, status and body
	 */
	public static ResponseEntity<String> errorMessage(String message, HttpStatus status ){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);

		return ResponseEntity.status(status).headers(headers).body(message);
	}
	
	
	/**
	 * It handles all type mismatch exception.
	 * 
	 * @param exception
	 * @param request
	 * @return ResponseEntity
	 */
	@ExceptionHandler(TypeMismatchException.class)
	@ResponseBody
	public ResponseEntity typeMismatchExpcetionHandler(Exception exception, HttpServletRequest request) {
	    return errorMessage("Invalid amount. Please input valid amount", HttpStatus.BAD_REQUEST);
	}

	/**
	 * It handles all missing paramater exception.
	 * @param exception
	 * @param request
	 * @return ResponseEntity
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseBody
	public ResponseEntity missingParameterExceptionHandler(Exception exception, HttpServletRequest request) {
		 return errorMessage("Error: "+exception.getMessage(), HttpStatus.BAD_REQUEST);
	}

	
	
	/**
	 * It handles all invalid format exception.
	 * 
	 * @param exception
	 * @param request
	 * @return ResponseEntity
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseBody
	public ResponseEntity invalidFormatExceptionHandler(HttpMessageNotReadableException exception, HttpServletRequest request) {
		return errorMessage("Error : invalid format", HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/**
	 * It handles all general exception.
	 * 
	 * @param exception
	 * @param request
	 * @return ResponseEntity
	 */
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public ResponseEntity generalExceptionHandler(Exception exception, HttpServletRequest request) {
		return errorMessage("Error : "+exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
}