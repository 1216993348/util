package com.xhy.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xhy.util.RepeatedlyReadRequestWrapper;

public class RepeatedlyReadFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(RepeatedlyReadFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		logger.debug("复制request.getInputStream流");
		HttpServletRequest copyRequest = null;
		System.out.println("复制request："+request);
		if (request instanceof HttpServletRequest) {
			copyRequest = new RepeatedlyReadRequestWrapper((HttpServletRequest) request);//替换成可重复获取InputStream的request子类对象
		}
		if (null == copyRequest) {
			chain.doFilter(request, response);
		} else {
			chain.doFilter(copyRequest, response);
		}
	}

	@Override
	public void destroy() {

	}
}
