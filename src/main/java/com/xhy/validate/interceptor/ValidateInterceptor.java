package com.xhy.validate.interceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xhy.entity.BaseResponse;
import com.xhy.util.string.ValidateUtil;

/**
 * 
 * @ClassName: ValidateInterceptor
 * @Description:数据校验工具类
 * @author Administrator
 * @date 2018年6月10日
 * 
 */
public class ValidateInterceptor implements HandlerInterceptor {
	private MessageSource source;
	private boolean isPrintError = false;
	private boolean isRest = true;

	public ValidateInterceptor() {
	}

	public boolean ifExistsMessage(String ruleKey) {
		try {
			this.source.getMessage(ruleKey, null, Locale.getDefault());
		} catch (NoSuchMessageException e) {
			// 没有发现验证规则会抛出此异常
			return false;
		}
		return true;
	}

	public ValidateInterceptor(MessageSource source, boolean isRest) {
		this.source = source;
		this.isRest = isRest;
	}

	public ValidateInterceptor(MessageSource source) {

		this.source = source;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if (handler instanceof HandlerMethod) {
			HandlerMethod hm = (HandlerMethod) handler;
			/*
			 * 进行参数验证
			 */
			// 拼凑验证规则名称，如：NewsAction.insert
			String className = hm.getBean().getClass().getSimpleName();
			// System.out.println("className : " + className);
			if (className != null && className.contains("$")) {// 如果使用了shiro那么控制器会被代理，而代理类名称中会有“$”
				className = className.substring(0, className.indexOf("$"));
			}
			String ruleKey = className + "." + hm.getMethod().getName();
			Map<String, String> errors = new HashMap<String, String>();// 将错误信息保存到map集合中
			// 从Validators.properties文件中取出验证规则,如果没有取出验证规则会抛出异常
			if (ifExistsMessage(ruleKey)) {// 如果存在对应的验证规则
				String rules = this.source.getMessage(ruleKey, null, Locale.getDefault());
				response.setCharacterEncoding("UTF-8");
				request.setCharacterEncoding("UTF-8");
				if (rules.startsWith("json:")) {// 如果是使用body传递json形式的参数
					// String rule = rules.
					ValidateUtil.validateValueByJson(rules.substring("json:".length()), getBodyString(request), errors, source);
				} else {// 否则是普通参数如：rules=name:string|id:int[]...逐个验证
					String result[] = rules.split("\\|");
					for (int x = 0; x < result.length; x++) {
						if (result[x].contains("[]")) {//判断是否是数组
							ValidateUtil.validateArray(result[x], request.getParameterValues(result[x].split(":")[0]+"[]"), errors, source);
						} else {//否则按照单值验证
							ValidateUtil.validateSingle(result[x], request.getParameter(result[x].split(":")[0]), errors, source);
						}

					}
				}
			}
			/*
			 * 进行文件验证
			 */
			// 拼凑文件上传规则的key
			String mimeRuleKey = ruleKey + "Mime";
			// System.out.println(mimeRuleKey);
			if (ifExistsMessage(mimeRuleKey)) {
				String mimerules = this.source.getMessage(mimeRuleKey, null, Locale.getDefault());
				// 如果含有文件上传的规则，则继续判断request请求是否正确，若不正确则添加错误信息
				if (mimerules != null) {
					if (request.getContentType() == null || !request.getContentType().contains("multipart/form-data")) {
						errors.put("requestError", source.getMessage("request.error.msg", null, null));
					}
				}
				// if (mr.isMultipart(request)) {// 判断是否有文件上传
				MultipartRequest mreq = (MultipartRequest) request;// 为了取得上传表单内容
				Map<String, MultipartFile> map = mreq.getFileMap();// 取得所有上传文件
				Iterator<Map.Entry<String, MultipartFile>> iter = map.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, MultipartFile> entry = iter.next();
					MultipartFile file = entry.getValue();
					// System.out.println("file:" + file);
					// System.out.println("fileName:" + entry.getKey());
					// System.out.println(file.getContentType());
					if (file.getSize() > 0) {// 如果文件大小大于零
						if (mimerules.contains(file.getContentType())) {// 符合上传类型规则
							// 继续判断是否满足大小
							String temp[] = file.getContentType().split("/");// 对mime拆分：如image/jpg 拆分为image，jpg
							long maxSize = Long.parseLong(source.getMessage(temp[0] + ".max", null, null));
							if (file.getSize() > maxSize) {// 文件大小超过了指定范围
								errors.put("maxSize",
										source.getMessage("file.max.error", new Object[] { maxSize }, null));
							}
						} else {// 如果不符合上传类型规则
							errors.put("unallowedMIME",
									source.getMessage("file.mime.error", new Object[] { mimerules }, null));
						}
					}
				}
			}
			if (errors.size() > 0) {
				try {
					isPrintError = Boolean.parseBoolean(source.getMessage("isPrintError", null, null));
				} catch (Exception e) {
					isPrintError = true;
				}
				if (!isPrintError) {
					errors = null;
				}
				if (isRest) {
					ObjectMapper mapper = new ObjectMapper();
					response.setContentType("text/json; charset=utf-8");
					BaseResponse baseResponse = new BaseResponse();
					baseResponse.setResult(errors);
					baseResponse.setState(false);
					baseResponse.setMessage("参数类型违法！");

					response.setStatus(HttpStatus.BAD_REQUEST.value());// 返回400错误
					response.getWriter().print(mapper.writeValueAsString(baseResponse));
				} else {
					request.setAttribute("errors", errors);
					request.getRequestDispatcher(source.getMessage("errors.page", null, Locale.getDefault()))
							.forward(request, response);// 或者跳转错误页
				}
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object obj, ModelAndView arg3)
			throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object obj, Exception exc)
			throws Exception {
	}

	 private static String getBodyString(HttpServletRequest request) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder("");
		try {
			if(request == null) {
				return null;
			}
			System.out.println("request:" +request);
			br = request.getReader();
			String str;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
}
