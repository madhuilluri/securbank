package securbank.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import securbank.exceptions.Exceptions;

@ControllerAdvice
public class GlobalExceptionController {

	@ExceptionHandler(Exceptions.class)
	public ModelAndView handleCustomException(Exceptions ex) {
		String tPath="error/genericError";
		ModelAndView model = new ModelAndView(tPath);
		if (ex.getErrMsg()==" ")
		{
			if (ex.getErrCode() == "400")
			{
				model.addObject("errCode", "400");
				model.addObject("errMsg", "Bad Request !");
				
			}else if ( ex.getErrCode()=="401")
			{
				model.addObject("errCode", "401");
				model.addObject("errMsg", "Unauthorized Access !");				
			}
			else if ( ex.getErrCode()=="404")
			{
				model.addObject("errCode", "404");
				model.addObject("errMsg", "Page Not Found !");				
			}
			else if ( ex.getErrCode()=="409")
			{
				model.addObject("errCode", "409");
				model.addObject("errMsg", "Conflit Occured !");					
			}
			else if (ex.getErrCode()=="500")
			{
				model.addObject("errCode", "500");
				model.addObject("errMsg", "Internal Server Error !");					
			}
			else 
			{
				model.addObject("errCode", ex.getErrCode());
				model.addObject("errMsg", "Bad Request !");
			}
		}else 
		{
			model.addObject("errCode", ex.getErrCode());
			model.addObject("errMsg", ex.getErrMsg());
		}
		return model;

	}

	@ExceptionHandler(Exception.class)
	public ModelAndView handleAllException(Exception ex) {

		ModelAndView model = new ModelAndView("error/genericError");
		model.addObject("400", "Bad Request");

		return model;
	}
}
