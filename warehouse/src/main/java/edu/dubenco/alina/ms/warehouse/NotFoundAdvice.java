package edu.dubenco.alina.ms.warehouse;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This class is used to return to the client the HTTP code 404 (Not Found) whenever NoSuchElementException is thrown.
 * 
 * @author Alina Dubenco
 *
 */
@ControllerAdvice
public class NotFoundAdvice {
	  @ResponseBody
	  @ExceptionHandler(NoSuchElementException.class)
	  @ResponseStatus(HttpStatus.NOT_FOUND)
	  String employeeNotFoundHandler(NoSuchElementException ex) {
	    return ex.getMessage();
	  }
}
