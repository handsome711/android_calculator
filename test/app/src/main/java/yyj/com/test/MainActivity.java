package yyj.com.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {
    private TextView tv_result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_result = findViewById(R.id.tv_result);
    }

    private final String symbol = "+-×÷()^"; //运算符
    private final String[] priority = {"+-", "×÷", "^", "()"}; //运算符优先级
    private int lastBtnId = 0;    //recode the last button we press

    public void click_clear(View v) {
        tv_result.setText("");
        lastBtnId = v.getId();
    }

    public void click_cancer(View v) {
        String text = tv_result.getText().toString();
        if (text.length() != 0)
            tv_result.setText(text.substring(0, text.length() - 1));
        lastBtnId = v.getId();
    }

    public void click_operator(View v) {
        int resid = v.getId();
        String text = tv_result.getText().toString();
        if (text.length() == 0)
            return;
        String operators = "+-×÷^";
        if (operators.indexOf(getLastDigit(text)) >= 0)
            click_cancer(v);
        switch (resid) {
            case R.id.btn_plus      : tv_result.append("+"); break;
            case R.id.btn_minus     : tv_result.append("-"); break;
            case R.id.btn_multiply  : tv_result.append("×"); break;
            case R.id.btn_divide    : tv_result.append("÷"); break;
        }
        lastBtnId = resid;
    }

    public void click_dot(View v) {
        String text = tv_result.getText().toString();
        int len = text.length();
        if (len == 0)
            tv_result.setText("0");
        else if (symbol.indexOf(getLastDigit(text)) >= 0)
            return;

        for (int i = len - 1; i >= 0; i--) {
            if (text.charAt(i) == '.')
                return;
            else if (symbol.indexOf(text.charAt(i)) >= 0)
                break;
        }
        tv_result.append(".");
        lastBtnId = v.getId();
    }

    public void click_operator_equal(View v) {
        try {
            String text = tv_result.getText().toString();
            if (text.length() == 0)
                return;
            else if (symbol.indexOf(getLastDigit(text)) >= 0)
                click_cancer(v);

            LinkedList<String> list   = seperate(text);
            LinkedList<String> suffix = transferToSuffix(list);
            double answer = calculate(suffix);
            String ans = simplify(answer);
            tv_result.setText(ans);
        } catch (Exception e) {
            Toast.makeText(this,
                    "please check your equation", Toast.LENGTH_SHORT).show();
            tv_result.setText("");
        } finally {
            lastBtnId = v.getId();
        }
    }

    public void click_num(View v) {
        if (lastBtnId == R.id.btn_equal)
            tv_result.setText("");
        int resid = v.getId();
        if (delete_0())
            click_cancer(v);
        switch (resid) {
            case R.id.btn_0 : tv_result.append("0"); break;
            case R.id.btn_1 : tv_result.append("1"); break;
            case R.id.btn_2 : tv_result.append("2"); break;
            case R.id.btn_3 : tv_result.append("3"); break;
            case R.id.btn_4 : tv_result.append("4"); break;
            case R.id.btn_5 : tv_result.append("5"); break;
            case R.id.btn_6 : tv_result.append("6"); break;
            case R.id.btn_7 : tv_result.append("7"); break;
            case R.id.btn_8 : tv_result.append("8"); break;
            case R.id.btn_9 : tv_result.append("9"); break;
        }
        lastBtnId = resid;
    }

    // 删除数字之前的0（像01）
    private boolean delete_0() {
        String text = tv_result.getText().toString();
        int len = text.length();
        if (len != 0 && getLastDigit(text) == '0') {
            if (len == 1 || symbol.indexOf(text.charAt(len - 2)) >= 0)
                return true;
        }
        return false;
    }

    private char getLastDigit(String text) {
        return text.charAt(text.length() - 1);
    }

    private LinkedList<String> seperate(String equation) {
        LinkedList<String> list=new LinkedList<>();
        StringBuilder buf = new StringBuilder();
        for (char c : equation.toCharArray()) {
            if (symbol.indexOf(c) >= 0) { //如果是运算符
                if (buf.length() > 0) {    //如果有操作数
                    String v = buf.toString();
                    list.add(v);
                    buf.delete(0, buf.length());
                } else if (c == '-') {    //负数
                    if (list.size() == 0 || !list.getLast().equals(")")) {
                        buf.append(c);
                        continue;
                    }
                }
                list.add(String.valueOf(c));
            } else
                buf.append(c);
        }
        if (buf.length() > 0)
            list.add(buf.toString());
        return list;
    }

    //中缀表达式转为后缀表达式
    private LinkedList<String> transferToSuffix(LinkedList<String> list) {
        LinkedList<String> operators = new LinkedList<>();  //用于记录操作符
        LinkedList<String> suffix    = new LinkedList<>(); //用于展示后缀表达式

        for (String s : list) {
            if (isOperator(s)) {
                if (operators.isEmpty())
                    operators.push(s);
                else {
                    //如果读入的操作符是")"，则弹出从栈顶开始第一个"("及其之前的所有操作符
                    if (s.equals(")")) {
                        while (!operators.peek().equals("(")) {
                            String operator = operators.pop();
                            suffix.add(operator);
                        }
                        operators.pop(); //弹出"("
                    } else if (priority(operators.peek()) < priority(s))
                        operators.push(s);
                    else {
                        //如果读入的操作符非")"且优先级比栈顶元素的优先级高或一样，则将操作符压入栈
                        while (operators.size() != 0 && priority(operators.peek()) >= priority(s)
                                && !operators.peek().equals("(")) {
                            String operator = operators.pop();
                            suffix.add(operator);
                        }
                        operators.push(s);
                    }
                }
            } else //读入的为非操作符
                suffix.add(s);
        }
        if (!operators.isEmpty()) {
            Iterator<String> iterator=operators.iterator();
            while (iterator.hasNext()) {
                String operator=iterator.next();
                suffix.add(operator);
                iterator.remove();
            }
        }
        return suffix;
    }

    //根据后缀表达式计算结果
    private double calculate(LinkedList<String> suffix){
        LinkedList<String> mList=new LinkedList<>();
        for (String s:suffix) {
            if (isOperator(s)){
                if (!mList.isEmpty()){
                    double num1=Double.valueOf(mList.pop());
                    double num2=Double.valueOf(mList.pop());
                    if (num1 == 0 && s.equals("÷")) {
                        Toast.makeText(this,
                                "the divisor cannot be 0",Toast.LENGTH_SHORT).show();
                        return 0;
                    }
                    double newNum=cal(num2,num1,s);
                    mList.push(String.valueOf(newNum));
                }
            } else //数字则压入栈中
                mList.push(s);
        }
        return Double.parseDouble(mList.pop());
    }

    //删除小数点后多余的0 / 删除小数点
    private String simplify(double answer) {
        String text = String.valueOf(answer);
        if (text.indexOf('.') < 0)
            return text;
        char digit = getLastDigit(text);
        while (digit == '0') {
            text = text.substring(0, text.length() - 1);
            digit = getLastDigit(text);
        }
        if (digit == '.')
            text = text.substring(0, text.length() - 1);
        return text;
    }

    //判断是否操作符
    private boolean isOperator(String oper) {
        return (symbol.contains(oper));
    }

    //计算操作符的优先级
    private int priority(String s){
        for (int i = 0; i < priority.length; i++) {
            if (priority[i].contains(s))
                return i;
        }
        return 0;
    }

    private static double cal(double num1, double num2, String operator){
        switch (operator){
            case "+":  return num1 + num2;
            case "-":  return num1 - num2;
            case "×": return num1 * num2;
            case "÷": return num1 / num2;
            case "^":  return Math.pow(num1, num2);
            default :  return 0;
        }
    }
}
