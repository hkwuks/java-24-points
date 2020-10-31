/**
 * @Description:
 * @Author:hkwuks
 * @Date:2020/10/28/20:57
 * @Version:1.0
 */
import java.util.*;

public class Game24Player{
    final String[] patterns={"nnonnoo","nnonono","nnnoono","nnnonoo","nnnnooo"};//所有可能的后缀运算表达式类型
    final String ops="+-*/^";

    String solution;
    List<Integer> digits;

    public static void main(String[] args){
        new Game24Player().play();
    }

    void play(){
        //生成有解的4个数和对应解
        digits=getSolvableDigits();

        Scanner in=new Scanner(System.in);
        while(true){
            System.out.print("Make 24 using these digits: ");
            System.out.println(digits);
            System.out.println("Enter 'q' to quit,'s'for a sulution");
            System.out.print(">");

            String line=in.nextLine();
            if(line.equalsIgnoreCase("q")){
                System.out.println("\nThanks for playing!");
                return;
            }

            if(line.equalsIgnoreCase("s")){
                System.out.println(solution);
                digits=getSolvableDigits();
                continue;
            }

            //开始校验输入表达式是否正确
            char[] entry=line.replaceAll("[^*+-/)(\\d]","").toCharArray();

            try{
                //判断输入的表达式格式是否正确
                validate(entry);

                //如果entry符合24点规则
                if(evaluate(infixToPostfix(entry))){
                    System.out.println("\nCorrect!Want to try another?");
                    digits=getSolvableDigits();
                }else{
                    System.out.println("\nNot correct.");
                }
            }catch(Exception e){
                System.out.printf("%n%s Try again.%n",e.getMessage());
            }
        }
    }

    //判断输入的表达式格式是否正确
    void validate(char[] input)throws Exception{
        int total_1=0,parens=0,opsCount=0;

        for(char c:input){
            if(Character.isDigit(c))
                total_1+=1<<(c-'0')*4;
            else if(c=='(')
                ++parens;
            else if(c==')')
                --parens;
            else if(ops.indexOf(c)!=-1)//计数运算操作符种类数
                ++opsCount;
            if(parens<0)
                throw new Exception("Parentheses mismatch.");
        }

        if(parens!=0)
            throw new Exception("Parentheses mismatch.");

        if(opsCount!=3)
            throw new Exception("Wrong number of operators.");

        int total_2=0;
        for(int d:digits)
            total_2+=1<<d*4;

        if(total_2!=total_1)
            throw new Exception("Not the same digits.");
    }

    //判断是否符合24点规则，传入的是后缀表达式
    boolean evaluate(char[] line)throws Exception{
        Stack<Float> s=new Stack<>();
        try{
            for(char c:line){
                if('0'<=c&&c<='9')
                    s.push((float)c-'0');
                else
                    s.push(applyOperator(s.pop(),s.pop(),c));
            }
        }catch(Exception e){
                throw new Exception("Invalid entry.");
            }
            return (Math.abs(24-s.peek())<0.001F);
        }

    //对栈中的运算进行处理
    float applyOperator(float a,float b,char c){
        switch(c){
            case '+':
                return a+b;
            case '-':
                return b-a;
            case '*':
                return a*b;
            case '/':
                return b/a;
            default:
                return Float.NaN;
        }
    }

    //产生4个随机数
    List<Integer> randomDigits(){
        Random r=new Random();
        List<Integer> result=new ArrayList<>(4);
        for(int i=0;i<4;++i)
            result.add(r.nextInt(9)+1);
        return result;
    }

    //产生有解的4个数
    List<Integer>getSolvableDigits(){
        List<Integer> result;
        do{
            result=randomDigits();
        }while(!isSolvable(result));
        return result;
    }

    //判断是否有解
    boolean isSolvable(List<Integer> digits){
        Set<List<Integer>> dPerms=new HashSet<>(4*3*2);
        permute(digits,dPerms,0);//产生不同的数字排列

        int total=4*4*4;//所有的运算排列总数
        List<List<Integer>>oPerms=new ArrayList<>(total);
        permuteOperators(oPerms,4,total);//所有的运算排列

        StringBuilder sb=new StringBuilder(4+3);

        for(String pattern:patterns){
            char[] patternChars=pattern.toCharArray();

            for(List<Integer> dig:dPerms){
                for(List<Integer> opr:oPerms){

                    int i=0,j=0;
                    for(char c:patternChars){
                        if(c=='n')
                            sb.append(dig.get(i++));
                        else
                            sb.append(ops.charAt(opr.get(j++)));
                    }

                    String candidate=sb.toString();
                    try {
                        //如果candidate符合24点的规则，就转为中缀表达式
                        if (evaluate(candidate.toCharArray())) {
                            solution = postfixToInfix(candidate);
                            return true;
                        }
                    }catch(Exception ignored){}
                    }
                    sb.setLength(0);
                }
            }
        return false;
    }

    //后缀表达式转中缀表达式
    String postfixToInfix(String postfix){
        class Expression{
            String op,ex;
            int prec=3;//prec

            Expression(String e){
                ex=e;
            }

            Expression(String e1,String e2,String o){
                ex=String.format("%s %s %s",e1,o,e2);
                op=o;
                prec=ops.indexOf(o)/2;
            }
        }

        Stack<Expression>expr=new Stack<>();

        for(char c:postfix.toCharArray()){
            int idx=ops.indexOf(c);
            //如果c是运算符
            if(idx!=-1){
                Expression r=expr.pop();
                Expression l=expr.pop();

                int opPrec=idx/2;

                //如果l子式的运算优先级低，就上括号
                if(l.prec<opPrec)
                    l.ex='('+l.ex+')';

                //如果r子式的运算优先级低，就上括号
                if(r.prec<=opPrec)
                    r.ex='('+r.ex+')';

                //将l子式和r子式连接起来
                expr.push(new Expression(l.ex,r.ex,""+c));
            }else {
                expr.push(new Expression(""+c));
            }
        }
        return expr.peek().ex;
    }

    //中缀表达式转后缀表达式
    char[] infixToPostfix(char[] infix)throws Exception{
        StringBuilder sb=new StringBuilder();
        Stack<Integer>s=new Stack<>();
        try {
            for(char c:infix){
                int idx=ops.indexOf(c);
                if(idx!=-1){
                    if(s.isEmpty())
                        s.push(idx);
                    else{
                        while (!s.isEmpty()){
                            int prec2=s.peek()/2;
                            int prec1=idx/2;
                            if(prec2>=prec1)
                                sb.append(ops.charAt(s.pop()));
                            else
                                break;
                        }
                        s.push(idx);
                    }
                }
                else if(c=='(')
                    s.push(-2);
                else if(c==')'){
                    while(s.peek()!=-2)
                        sb.append(ops.charAt(s.pop()));
                    s.pop();
                }
                else
                    sb.append(c);
            }
            while (!s.isEmpty())
                sb.append(ops.charAt(s.pop()));
        }catch (EmptyStackException e){
            throw new Exception("Invalid entry.");
        }
        return sb.toString().toCharArray();
    }

    //产生不同的排列
    void permute(List<Integer>lst,Set<List<Integer>>res,int k){
        for(int i=k;i<lst.size();++i){
            Collections.swap(lst,i,k);
            permute(lst,res,k+1);
            Collections.swap(lst,k,i);
        }
        if(k==lst.size())
            res.add(new ArrayList<>(lst));
    }

    //产生所有的运算排列
    void permuteOperators(List<List<Integer>> res,int n,int total){
        for(int i=0,npow=n*n;i<total;++i)
            res.add(Arrays.asList((i/npow),(i%npow)/n,i%n));
    }
}
