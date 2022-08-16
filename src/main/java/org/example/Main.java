package org.example;
import java.util.*;
import java.sql.*;
public class Main {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        //Объект класса Skanner для ввода выражения

        Scanner s = new Scanner(System.in);
        System.out.println("Enter expression or press ENTER to recalculate existing expressions");
        String scan= s.nextLine();
        Double result = null;
        String name = "root";
        String password = "root";
        String URL = "jdbc:mysql://localhost:3306/my_test";
        String SQL = "";
        //Class.forName("com.mysql.cj.jdbc.Driver");

        //Если выражение корректно, то вычисляем значение

        if (scan!=""&&isValid(scan)){
            System.out.println("Expression: "+scan);
            List<Lexeme> lexemes = lexAnalyze(scan);
            LexemeBuffer lexemeBuffer = new LexemeBuffer(lexemes);
            result = expr(lexemeBuffer);
            System.out.println("result = " + result);
            System.out.println();

        //Подключаемся к базе данных

        try {Connection connection = DriverManager.getConnection(URL, name, password);
                    SQL = String.format("INSERT INTO new_table (expressions, result)" +
                    "VALUES ('%s', '%s')", scan, result.toString());
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(SQL);
            connection.close();
        }
        catch (SQLException e){
            e.printStackTrace();
        }}

        //иначе произведем пересчет существующих в таблице выражений, которые могли измениться

            else if (scan=="") {
                Connection connection = DriverManager.getConnection(URL, name, password);
                SQL = "SELECT expressions FROM new_table";
                PreparedStatement prepStat = connection.prepareStatement(SQL);
                ResultSet rs = prepStat.executeQuery();
                while (rs.next()){
                String str = rs.getString("expressions");

                if (isValid(str)){
                    System.out.println("Expression: "+str);
                    List<Lexeme> lexemes = lexAnalyze(str);
                    LexemeBuffer lexemeBuffer = new LexemeBuffer(lexemes);
                    result = expr(lexemeBuffer);
                    System.out.println("result = " + result);
                    System.out.println();
                    String update = String.format("UPDATE new_table " + " SET result = ? " +
                        "WHERE expressions = ?");
                    PreparedStatement prepUpdate = connection.prepareStatement(update);
                    prepUpdate.setString(1,result.toString());
                    prepUpdate.setString(2,str);
                    prepUpdate.executeUpdate();
            }
                 else {  System.out.println("Expression: " + str + " - is not correct!");
                        break;
                 }
            }
            rs.close();
            connection.close();

            //вызываем метод поиска выражений равных 6.0

            System.out.println();
            System.out.println("Expressions with result = 6.0");
            search();
        }
           else  {
            System.out.println("Expression: " + scan + " - is not correct!");}
    }

    public static boolean isValid(String str) {
        // Определяем количество цифр в заданном выражении
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                count++;
            }
        }
        System.out.println("Number of digits in expression = " + count);
        //Создаем множество и помещаем в него знаки арифметических операций
        HashSet<Character> signs = new HashSet<>();
        signs.add('+');
        signs.add('-');
        signs.add('*');
        signs.add('/');
        //Проверка первого символа на корректность
        char[] arrCh = str.toCharArray();
        if (arrCh[0]=='+'||arrCh[0]=='/'||arrCh[0]=='*')
        {return false;}
        //Проверка последнего символа на корректность
        if(signs.contains(arrCh[arrCh.length-1]))
        {return false;}
        //проверка знаков внутри строки до предпоследнего символа,
        //т.к. последний уже проверили
        for (int i = 1; i < arrCh.length-1; i++) {
            if (signs.contains(arrCh[i-1])&&signs.contains(arrCh[i])&&arrCh[i]!='-')
            {return false;}
        }
        Map<Character, Character> brac = new HashMap<>();//мапа для проветки скобок
        brac.put(')', '(');
        //создаем очередь типа стек(первый пришел-последний ушел)
        Deque<Character> stack = new LinkedList<>();
        //пробегаемся по массиву, делаем проверку
        for (Character c : arrCh) {
            if (brac.containsValue(c)) stack.push(c);
            else if (brac.containsKey(c)) {
                if (stack.isEmpty() || stack.pop() != brac.get(c)) {
                    return false;
                }
            }
        }

        return stack.isEmpty();
    }
//перечисление типов символов
public enum LexemeType {
    LEFT_BRACKET, RIGHT_BRACKET,
    OP_PLUS, OP_MINUS, OP_MUL, OP_DIV,
    NUMBER,
    EOF;
}
//Создаем класс Lexeme с 2мя конструкторами и переопределим метод toString
public static class Lexeme {
    LexemeType type;
    String value;
    public Lexeme(LexemeType type, String value) {
        this.type = type;
        this.value = value;
    }
    public Lexeme(LexemeType type, Character value) {
        this.type = type;
        this.value = value.toString();
    }
    @Override
    public String toString() {
        return "Lexeme{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
//все данные согласно прохода по массиву сконцентрируем в классе LexemeBuffer
    public static class LexemeBuffer {
        private int pos;
        public List<Lexeme> lexemes;
        public LexemeBuffer(List<Lexeme> lexemes) {
            this.lexemes = lexemes;
        }
        public Lexeme next() {
            return lexemes.get(pos++);
        }
        public void back() {
            pos--;
        }
        public int getPos() {
            return pos;
        }
    }
    //Проанализируем заданное выражение, и заполним массив
    public static List<Lexeme> lexAnalyze(String expText) {
        ArrayList<Lexeme> lexemes = new ArrayList<>();
        int pos = 0;
        while (pos< expText.length()) {
            char c = expText.charAt(pos);
            switch (c) {
                case '(':
                    lexemes.add(new Lexeme(LexemeType.LEFT_BRACKET, c));
                    pos++;
                    continue;
                case ')':
                    lexemes.add(new Lexeme(LexemeType.RIGHT_BRACKET, c));
                    pos++;
                    continue;
                case '+':
                    lexemes.add(new Lexeme(LexemeType.OP_PLUS, c));
                    pos++;
                    continue;
                case '-':
                    lexemes.add(new Lexeme(LexemeType.OP_MINUS, c));
                    pos++;
                    continue;
                case '*':
                    lexemes.add(new Lexeme(LexemeType.OP_MUL, c));
                    pos++;
                    continue;
                case '/':
                    lexemes.add(new Lexeme(LexemeType.OP_DIV, c));
                    pos++;
                    continue;
                default:
                    if (c <= '9' && c >= '0'|| c == '.') {
                        StringBuilder sb = new StringBuilder();
                        do {
                            sb.append(c);
                            pos++;
                            if (pos >= expText.length()) {
                                break;
                            }
                            c = expText.charAt(pos);
                        } while (c <= '9' && c >= '0'|| c == '.');
                        lexemes.add(new Lexeme(LexemeType.NUMBER, sb.toString()));
                    }
                    //если в строке будет пробел, не будем его учитывать
                    else {
                        if (c != ' ') {
                            //throw new RuntimeException("Unexpected character: " + c);
                        }
                        pos++;
                    }
            }
        }
        lexemes.add(new Lexeme(LexemeType.EOF, ""));
        return lexemes;
    }
    public static double expr(LexemeBuffer lexemes) {
        Lexeme lexeme = lexemes.next();
        if (lexeme.type == LexemeType.EOF) {
            return 0;
        } else {
            lexemes.back();
            return plusminus(lexemes);
        }
    }
//Вычисление выражения суммы и разности
    public static double plusminus(LexemeBuffer lexemes) {
        double value = multdiv(lexemes);
        while (true) {
            Lexeme lexeme = lexemes.next();
            switch (lexeme.type) {
                case OP_PLUS:
                    value += multdiv(lexemes);
                    break;
                case OP_MINUS:
                    value -= multdiv(lexemes);
                    break;
                case EOF:
                case RIGHT_BRACKET:
                    lexemes.back();
                    return value;
                default:
                    throw new RuntimeException("Unexpected token: " + lexeme.value
                            + " at position: " + lexemes.getPos());
            }
        }
    }
    //Вычисление выражения умножения и деления
    public static double multdiv(LexemeBuffer lexemes) {
        double value = factor(lexemes);
        while (true) {
            Lexeme lexeme = lexemes.next();
            switch (lexeme.type) {
                case OP_MUL:
                    value *= factor(lexemes);
                    break;
                case OP_DIV:
                    value /= factor(lexemes);
                    break;
                case EOF:
                case RIGHT_BRACKET:
                case OP_PLUS:
                case OP_MINUS:
                    lexemes.back();
                    return value;
                default:
                    throw new RuntimeException("Unexpected token: " + lexeme.value
                            + " at position: " + lexemes.getPos());
            }
        }
    }
//Вычисление значения для чисел и внутри скобок
    public static double factor(LexemeBuffer lexemes) {
        Lexeme lexeme = lexemes.next();
        switch (lexeme.type) {
            case OP_MINUS:
                double value = factor(lexemes);
                return -value;
            case NUMBER:
                return Double.parseDouble(lexeme.value);
            case LEFT_BRACKET:
                value = plusminus(lexemes);
                lexeme = lexemes.next();
                if (lexeme.type != LexemeType.RIGHT_BRACKET) {
                    throw new RuntimeException("Unexpected token: " + lexeme.value
                            + " at position: " + lexemes.getPos());
                }
                return value;
            default:
                throw new RuntimeException("Unexpected token: " + lexeme.value
                        + " at position: " + lexemes.getPos());
        }
    }
    //Реализация поиска выражений в БД по их результатам
    public static void search () throws SQLException {
        String name = "root";
        String password = "root";
        String URL = "jdbc:mysql://localhost:3306/my_test";
        Connection connection = DriverManager.getConnection(URL, name, password);
        String SQL = "SELECT expressions FROM new_table WHERE result = '6.0'";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(SQL);

        while (rs.next()){
            System.out.println(rs.getString("expressions")+ " = 6.0");
        }
        rs.close();
        connection.close();
    }

}