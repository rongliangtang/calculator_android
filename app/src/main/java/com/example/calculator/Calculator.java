package com.example.calculator;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * @Title: Calculator.java
 * @desc: 加减乘除计算器，支持括号，小数，负数
 * @author mengchuan.li
 * @date 2016年11月14日 下午1:22:39
 */
public class Calculator {
    public String TAG = "Calculator";
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        Scanner s = new Scanner(System.in);
        System.out.println("请输入需要计算式子(输入end退出计算器)：如-1.1+2x3(4x(-6+5))÷(6(2+0.2)(7-3))=");
        String result;
        while (s.hasNextLine()) {
            String str = s.nextLine();
            if ("end".equals(str)) {
                System.out.println("谢谢使用!");
                break;
            }
            // 计算处理
            result = calc.prepareParam(str);
            if (result != null) {
                // 处理结果并打印
                System.out.println(result);
            }
        }

    }

    /**
     *
     * @Title: PrepareParam
     * @Desc: 准备计算的数据，符号
     *
     * @param: str计算式
     * @return: 计算结果
     *
     */
    public String prepareParam(String str) {
        // 空值校验
        if (null == str || "".equals(str)) {
            return null;
        }
        // 长度校验
        if (str.length() > MyUtils.FORMAT_MAX_LENGTH) {
            System.out.println("表达式过长！");
            return null;
        }
        // 预处理
        str = str.replaceAll(" ", "");// 去空格
        if ('-' == str.charAt(0)) {// 开头为负数，如-1，改为0-1
            str = 0 + str;
        }
        // 校验格式
        if (!MyUtils.checkFormat(str)) {
            System.out.println("表达式错误！");
            return "表达式错误";
        }
        // 处理表达式，改为标准表达式
        str = MyUtils.change2StandardFormat(str);
        // 拆分字符和数字
        String[] nums = str.split("[^.0-9]");
//        Log.d(TAG, nums[0] + "----" + nums[1]);
        List<Double> numLst = new ArrayList();
        for (int i = 0; i < nums.length; i++) {
            if (!"".equals(nums[i])) {
                numLst.add(Double.parseDouble(nums[i]));
            }
        }
        String symStr = str.replaceAll("[.0-9]", "");
        double result = doCalculate(symStr, numLst);

        return MyUtils.formatResult(String.format("%." + MyUtils.RESULT_DECIMAL_MAX_LENGTH + "f", result));
    }

    /**
     *
     * @Title: doCalculate
     * @Desc: 计算
     *
     * @param: symStr符号串
     * @param: numLst数字集合
     * @return 计算结果
     *
     */
    public Double doCalculate(String symStr, List<Double> numLst) {
        LinkedList<Character> symStack = new LinkedList<>();// 符号栈
        LinkedList<Double> numStack = new LinkedList<>();// 数字栈
        double result = 0;
        int i = 0;// numLst的标志位
        int j = 0;// symStr的标志位
        char sym;// 符号
        double num1, num2;// 符号前后两个数
        while (symStack.isEmpty() || !(symStack.getLast() == '=' && symStr.charAt(j) == '=')) {// 形如：
            // =8=
            // 则退出循环，结果为8
            if (symStack.isEmpty()) {
                symStack.add('=');
                numStack.add(numLst.get(i++));
            }
            if (MyUtils.symLvMap.get(symStr.charAt(j)) > MyUtils.symLvMap.get(symStack.getLast())) {// 比较符号优先级，若当前符号优先级大于前一个则压栈
                if (symStr.charAt(j) == '(') {
                    symStack.add(symStr.charAt(j++));
                    continue;
                }
                numStack.add(numLst.get(i++));
                symStack.add(symStr.charAt(j++));
            } else {// 当前符号优先级小于等于前一个 符号的优先级
                if (symStr.charAt(j) == ')' && symStack.getLast() == '(') {// 若（）之间没有符号，则“（”出栈
                    j++;
                    symStack.removeLast();
                    continue;
                }
                if (symStack.getLast() == '(') {// “（”直接压栈
                    numStack.add(numLst.get(i++));
                    symStack.add(symStr.charAt(j++));
                    continue;
                }
                num2 = numStack.removeLast();
                num1 = numStack.removeLast();
                sym = symStack.removeLast();
                switch (sym) {
                    case '+':
                        numStack.add(MyUtils.plus(num1, num2));
                        break;
                    case '-':
                        numStack.add(MyUtils.reduce(num1, num2));
                        break;
                    case 'x':
                        numStack.add(MyUtils.multiply(num1, num2));
                        break;
                    case '÷':
                        if (num2 == 0) {// 除数为0
                            System.out.println("存在除数为0");
                            return null;
                        }
                        numStack.add(MyUtils.divide(num1, num2));
                        break;
                }
            }
        }
        return numStack.removeLast();
    }

}