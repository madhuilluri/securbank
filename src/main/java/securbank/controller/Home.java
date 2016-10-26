package securbank.controller;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import securbank.view.CreatePDF;

/**
 * @author Ayush Gupta
 *
 */
@Controller
public class Home {
	
	String TAG = getClass().toString()+ " ";
	
	@RequestMapping("/")
    public String homeController(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model) {
       return "home";
    }
}
