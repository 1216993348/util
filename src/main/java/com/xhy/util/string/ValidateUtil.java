package com.xhy.util.string;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.springframework.context.MessageSource;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 提供有数据验证的方法
 * 
 * @author Administrator
 *
 */
public class ValidateUtil {
	/**
	 * 基于Spring MVC 拦截器进行的服务器端数据验证
	 * 
	 * @param rule
	 * @param request
	 * @param errors
	 * @param source
	 */
	public static void validateSingle(String rule, String data, Map<String, String> errors, MessageSource source) {
		String temp[] = rule.split(":");
		boolean isAllowNull = true;// 允许为null
		if (temp.length == 2 || "false".equalsIgnoreCase(temp[2])) {// 如果验证规则是rids:int[],即temp长度为2，则默认不能为null或空串
			// 当前验证的情况不允许为null
			isAllowNull = false;
		}
		// 按照单值处理
		// if(temp.length == 2 ||
		// "false".equals(temp[2])){//如果验证规则是news.nid:int,即temp长度为2，则默认不能为null或空串
		// System.out.print("不能为空 ");
		if ("int".equalsIgnoreCase(temp[1])) {
			if (!ValidateUtil.validateInt(data, isAllowNull)) {// 没通过验证！
				errors.put(temp[0], source.getMessage("int.validate.error.msg", null, null));// 添加错误信息！
			}
		} else if ("double".equalsIgnoreCase(temp[1])) {
			if (!ValidateUtil.validateDouble(data, isAllowNull)) {
				errors.put(temp[0], source.getMessage("double.validate.error.msg", null, null));// 添加错误信息！
			}
		} else if ("string".equalsIgnoreCase(temp[1])) {
			if (isAllowNull) {// isAllowNull如果是true,说明temp.length >2
								// 是范围验证:MessageController.insert=sid:string:1,2
				String range[] = temp[2].split(",");
				if (!ValidateUtil.validateString(data, Integer.parseInt(range[0]), Integer.parseInt(range[1]))) {// 没通过验证！
					errors.put(temp[0], source.getMessage("string.validate.error.msg", range, null));// 添加错误信息！
				}
			} else if (!ValidateUtil.validateString(data)) {// 不是范围验证那就验证不允许空或空串
				errors.put(temp[0], source.getMessage("string.validate.error.msg", null, null));// 添加错误信息！
			}
		} else if ("date".equalsIgnoreCase(temp[1])) {
			if (!ValidateUtil.validateDate(data, isAllowNull)) {
				errors.put(temp[0], source.getMessage("date.validate.error.msg", null, null));// 添加错误信息！
			}
		} else if ("long".equalsIgnoreCase(temp[1])) {
			if (!ValidateUtil.validateLong(data, isAllowNull)) {
				errors.put(temp[0], source.getMessage("long.validate.error.msg", null, null));// 添加错误信息！
			}
		}
		// 验证码通常交由业务程序处理暂且废弃此部分
		/*
		 * else if ("rand".equalsIgnoreCase(temp[1])) { String code =
		 * request.getParameter("code"); String rand = (String)
		 * request.getSession().getAttribute("rand"); if (rand == null ||
		 * !rand.equalsIgnoreCase(code)) { errors.put(temp[0],
		 * source.getMessage("rand.validate.error.msg", null, null));// 添加错误信息！ } }
		 */
	}

	public static void validateArray(String rule, String data[], Map<String, String> errors, MessageSource source) {
		String temp[] = rule.split(":");
		boolean isAllowNull = true;// 允许为null
		if (temp.length == 2 || "false".equalsIgnoreCase(temp[2])) {// 如果验证规则是rids:int[],即temp长度为2，则默认不能为null或空串
			// 当前验证的情况不允许为null
			isAllowNull = false;
		}
		if (!isAllowNull && data == null) {// 不允许为空的情况下数组为空了，则没有通过验证,添加错误信息
			if ("int[]".equalsIgnoreCase(temp[1])) {
				errors.put(temp[0], source.getMessage("int.validate.error.msg", null, null));// 添加错误信息！
			} else if ("double[]".equalsIgnoreCase(temp[1])) {
				errors.put(temp[0], source.getMessage("double.validate.error.msg", null, null));// 添加错误信息！
			} else if ("string[]".equalsIgnoreCase(temp[1])) {
				errors.put(temp[0], source.getMessage("string.validate.error.msg", null, null));// 添加错误信息！
			} else if ("date[]".equalsIgnoreCase(temp[1])) {
				errors.put(temp[0], source.getMessage("date.validate.error.msg", null, null));// 添加错误信息！
			} else if ("long[]".equalsIgnoreCase(temp[1])) {
				errors.put(temp[0], source.getMessage("long.validate.error.msg", null, null));// 添加错误信息！
			}
		} else {// 数组不为null，继续遍历数组验证
			if ("int[]".equalsIgnoreCase(temp[1])) {
				for (int x = 0; x < data.length; x++) {
					if (!ValidateUtil.validateInt(data[x], isAllowNull)) {// 没通过验证！
						errors.put(temp[0], source.getMessage("int.validate.error.msg", null, null));// 添加错误信息！
					}
				}
			} else if ("double[]".equalsIgnoreCase(temp[1])) {
				for (int x = 0; x < data.length; x++) {
					if (!ValidateUtil.validateDouble(data[x], isAllowNull)) {// 没通过验证！
						errors.put(temp[0], source.getMessage("double.validate.error.msg", null, null));// 添加错误信息！
					}
				}
			} else if ("string[]".equalsIgnoreCase(temp[1])) {
				for (int x = 0; x < data.length; x++) {
					if (isAllowNull) {// 如果验证规则是MessageController.insert=sid:string[]:1,2 则isAllowNull为true
						String range[] = temp[2].split(",");
						if (!ValidateUtil.validateString(data[x], Integer.parseInt(range[0]),
								Integer.parseInt(range[1]))) {// 没通过验证！
							errors.put(temp[0], source.getMessage("string.validate.error.msg", range, null));// 添加错误信息！
						}
					} else if (!ValidateUtil.validateString(data[x])) {// 否则验证规则是MessageController.insert=sid:string
																		// ,没有限制范围
						errors.put(temp[0], source.getMessage("string.validate.error.msg", null, null));// 添加错误信息！
					}
				}
			} else if ("date[]".equalsIgnoreCase(temp[1])) {
				for (int x = 0; x < data.length; x++) {
					if (!ValidateUtil.validateDate(data[x], isAllowNull)) {// 没通过验证！
						errors.put(temp[0], source.getMessage("date.validate.error.msg", null, null));// 添加错误信息！
					}
				}
			} else if ("long[]".equalsIgnoreCase(temp[1])) {
				for (int x = 0; x < data.length; x++) {
					if (!ValidateUtil.validateLong(data[x], isAllowNull)) {// 没通过验证！
						errors.put(temp[0], source.getMessage("long.validate.error.msg", null, null));// 添加错误信息！
					}
				}
			}
		}

	}

	public static Object getValueByKey(String key, Map<?, ?> map) throws RuntimeException {// 根据key取得value,key可以是：a.b.c.d类型
		String keyArray[] = key.split("\\.");
		Object currentObj = map;
		System.out.println(currentObj instanceof Map);
		for (int i = 0; i < keyArray.length; i++) {
			if (currentObj instanceof Map) {
				currentObj = ((Map<?, ?>) currentObj).get(keyArray[i]);
			} else if(currentObj == null) {
				return null;
			}else if (i < keyArray.length) {
				throw new RuntimeErrorException(null, "Key:'" + key + "'has an error, and'" + keyArray[i - 1]
						+ "' does not have an attribute'" + keyArray[i] + "'");
			}
		}
		return currentObj;
	}

	public static void main(String[] args) {
		try {
			Map map = new ObjectMapper().readValue("{\"user\":{\"dept\":{}}}", Map.class);
			System.out.println(getValueByKey("user.dept", map));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void validateValueByJson(String rules, String data, Map<String, String> errors,
			MessageSource source) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = new ObjectMapper().readValue(data, Map.class);
			boolean isAllowNull =true;
			String rule[] = rules.split("\\|");// 将多个验证规则拆分为单个，如：name:string|user.dept.cid:int
			for (int x = 0; x < rule.length; x++) {
				String temp[] = rule[x].split(":");
				if (temp.length == 2 || "false".equalsIgnoreCase(temp[2])) {// 如果验证规则是rids:int[],即temp长度为2，则默认不能为null或空串
					// 当前验证的情况不允许为null
					isAllowNull = false;
				}
				Object obj = getValueByKey(temp[0], map);
				if (rule[x].contains("[]")) {// 判断是否是数组
					if( !isAllowNull && obj==null) {
						errors.put(temp[0], source.getMessage("json.validate.error.msg", null, null));// 添加错误信息！
					}else if (obj instanceof List) {
						List<?> list = (List<?>) obj;
						for (Iterator<?> iter = list.iterator(); iter.hasNext();) {
							Object value = iter.next();
							validateObjectValue(rule[x],isAllowNull, value, errors, source);
						}
					} 
				} else {// 否则按照单值验证
					validateObjectValue(rule[x],isAllowNull, obj, errors, source);
				}

			}
		} catch (IOException e) {
			errors.put("jsonError", source.getMessage("json.validate.error.msg", null, null));// 添加错误信息！
		}
	}

	/**
	 * 验证value类型
	 * 
	 * @param name
	 *            错误信息的字段名称
	 * @param value
	 *            需要验证的值
	 * @param isAllowNull
	 *            是否允许为null
	 * @param errors
	 *            保存错误信息
	 * @param source
	 *            读取资源文件
	 */
	public static void validateObjectValue(String rule,boolean isAllowNull, Object value, Map<String, String> errors,
			MessageSource source) {
		String temp[] = rule.split(":");
		if (isAllowNull) {// 允许为空,针对字符串进行特殊处理
			if (temp[1].equalsIgnoreCase("string[]") || temp[1].equalsIgnoreCase("string")) {// 如果是字符串则是范围验证
				String range[] = temp[2].split(",");
				String str = String.valueOf(value);
				if (value instanceof String) {
					str = (String) value;
				} else {
					str = String.valueOf(value);
				}
				if (!ValidateUtil.validateString(str, Integer.parseInt(range[0]), Integer.parseInt(range[1]))) {// 没通过验证！
					errors.put(temp[0], source.getMessage("string.validate.error.msg", range, null));// 添加错误信息！
				}
			} else if (value == null) {// 【1】除了字符串之外 ，其他类型在isAllowNull=true 情况下允许为null,无需验证
				return;
			}
		} else if (temp[1].equalsIgnoreCase("string[]") || temp[1].equalsIgnoreCase("string")) {
			String str = String.valueOf(value);
			if (value instanceof String) {
				str = (String) value;
			} else {
				str = String.valueOf(value);
			}
			if (!ValidateUtil.validateString(str)) {// 没通过验证！
				errors.put(temp[0], source.getMessage("string.validate.error.msg", null, null));// 添加错误信息！
			}
			return;
		}
		// 除了【1】特殊情况，其余情况统一处理
		if (temp[1].equalsIgnoreCase("int[]") || temp[1].equalsIgnoreCase("int")) {// 判断是否是int型
			if (!(value instanceof Integer)) {
				if (value instanceof String) {// 如果是string仍不能转换为int
					String str = (String) value;
					if (!validateInt(str, isAllowNull)) {
						errors.put(temp[0], source.getMessage("int.validate.error.msg", null, null));// 添加错误信息！
					}
				} else {
					errors.put(temp[0], source.getMessage("int.validate.error.msg", null, null));// 添加错误信息！
				}

			}
		} else if (temp[1].equalsIgnoreCase("double[]") || temp[1].equalsIgnoreCase("double")) {// 判断是否是double型
			if (!(value instanceof Double) || value.equals(Double.NEGATIVE_INFINITY)
					|| value.equals(Double.NEGATIVE_INFINITY) || value.equals(Double.NaN)) {
				if (value instanceof String) {// 如果是string仍不能转换为double
					String str = (String) value;
					if (!validateDouble(str, isAllowNull)) {
						errors.put(temp[0], source.getMessage("double.validate.error.msg", null, null));// 添加错误信息！
					}
				} else {
					errors.put(temp[0], source.getMessage("double.validate.error.msg", null, null));// 添加错误信息！
				}

			}
		} else if (temp[1].equalsIgnoreCase("long[]") || temp[1].equalsIgnoreCase("long")) {// 判断是否是long型,包括Integer
			if (!(value instanceof Integer) || !(value instanceof Long) || !(value instanceof BigInteger)) {
				if (value instanceof String) {// 如果是string仍不能转换为long
					String str = (String) value;
					if (!validateLong(str, isAllowNull)) {
						errors.put(temp[0], source.getMessage("long.validate.error.msg", null, null));// 添加错误信息！
					}
				} else {
					errors.put(temp[0], source.getMessage("long.validate.error.msg", null, null));// 添加错误信息！
				}
			}
		} else if (temp[1].equalsIgnoreCase("date[]")) {
			String str = String.valueOf(value);
			if (value instanceof String) {
				str = (String) value;
				if (!ValidateUtil.validateDate(str, isAllowNull)) {// 没通过验证！
					errors.put(temp[0], source.getMessage("date.validate.error.msg", null, null));// 添加错误信息！
				}
			} else if (!(value instanceof Integer) && !(value instanceof Long)) {
				errors.put(temp[0], source.getMessage("date.validate.error.msg", null, null));// 添加错误信息！
			}

		}
	}

	/**
	 * 验证字符串是否满足长度范围，默认验证是否为空
	 * 
	 * @param str
	 *            被验证的字符串
	 * @param range
	 *            表示字符串长度范围，range[0]为最小长度，range[1]为最大长度，闭区间
	 * @return 如果字符串不为空且长度不为零,或者满足长度范围返回true,否则返回flase
	 */
	public static boolean validateString(String str, Integer... range) {
		// 如果没有规定范围，则不允许为null和空串
		if (range == null || range.length != 2) {
			return str != null && !"".equals(str);
		} else {
			if (range[0] == -1) {// 长度范围如果包含-1，则可以为null 和空串
				return str == null || str.length() <= range[1];
			} else if (range[0] == 0) {// 长度范围如果包含0，则可以为空串
				return str != null && str.length() <= range[1];
			} else {// 长度范围如果大于0，则不允许为null和空串
				return str != null && str.length() >= range[0] && str.length() <= range[1];
			}
		}
	}

	/**
	 * 进行字符串的正则验证
	 * 
	 * @param str
	 * @param regex
	 * @return
	 */
	public static boolean validateRegex(String str, String regex) {
		if (validateString(str)) {
			return str.matches(regex);
		} else {
			return false;
		}
	}

	/**
	 * 判断字符串是否可以转换为Int数据
	 * 
	 * @param str
	 * @param empty
	 * @return
	 */
	public static boolean validateInt(String str, boolean empty) {
		if (empty) {// 允许为空
			if (str == null || "".equals(str)) {
				return true;
			}
		}
		try {
			Integer.valueOf(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * 判断字符串是否可以转换为Double数据
	 * 
	 * @param str
	 * @param empty
	 * @return
	 */
	public static boolean validateDouble(String str, boolean empty) {
		if (empty) {// 允许为空
			if (str == null || "".equals(str)) {
				return true;
			}
		}
		try {
			if (validateString(str)) {
				Double.valueOf(str);
			} else {
				return false;
			}
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * 判断字符串是否可以转换为Date数据
	 * 
	 * @param str
	 * @param empty
	 * @return
	 */
	public static boolean validateDate(String str, boolean empty) {
		if (empty) {// 允许为空
			if (str == null || "".equals(str)) {
				return true;
			}
			if (str.matches("\\d{4}-\\d{2}-\\d{2}")) {
				return true;
			} else {
				return str.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
			}
		} else {
			if (ValidateUtil.validateString(str)) {
				if (str.matches("\\d{4}-\\d{2}-\\d{2}")) {
					return true;
				} else {
					return str.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
				}
			} else {
				return false;
			}
		}
	}

	/**
	 * 判断字符串是否可以转换为Long数据
	 * 
	 * @param str
	 * @param empty
	 * @return
	 */
	public static boolean validateLong(String str, boolean empty) {
		if (empty) {// 允许为空
			if (str == null || "".equals(str)) {
				return true;
			}
		}
		try {
			Long.valueOf(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

}
