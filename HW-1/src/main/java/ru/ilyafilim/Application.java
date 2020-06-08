package ru.ilyafilim;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Application {

    public static void main(String[] args) throws IOException {

        List<Integer> salaryList = new ArrayList<>();
        List<String> skillsList = new ArrayList<>();

        Set<String> spec = new HashSet<>();

        String url = "https://api.hh.ru/vacancies?text=Data+engineer&per_page=100&page=";

        int c = 0;
        JSONObject request = getJSONRequest(url + "1");
        int pageCount = request.getInt("pages");
        for (int i = 0; i != pageCount; i++) {
            if (i != 0) request = getJSONRequest(url + (i + 1));
            JSONArray jsonArray = request.getJSONArray("items");
            for (int a = 0; a < jsonArray.length(); a++) {
                JSONObject vacancy = getJSONRequest(jsonArray.getJSONObject(a).getString("url"));
                JSONArray specializations = vacancy.getJSONArray("specializations");
                for (int j = 0; j < specializations.length(); j++) {
                    spec.add(specializations.getJSONObject(j).getString("profarea_name"));
                }

                Object salary = vacancy.get("salary");
                salaryList.add(getSalary(salary));
                JSONArray skills = vacancy.getJSONArray("key_skills");
                for (int j = 0; j < skills.length(); j++) {
                    skillsList.add(skills.getJSONObject(j).getString("name"));
                }
                //System.out.println(skills);
                c++;
            }
        }
        System.out.println("count = " + c);
        System.out.println("not null salaries: " + salaryList.stream().filter(Objects::nonNull).count());
        System.out.println("max: " + salaryList.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).max());
        System.out.println("min: " + salaryList.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).min());
        System.out.println("ave: " + salaryList.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).average());
        System.out.println();
        Map<String, Long> stringLongMap = skillsList.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        stringLongMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(System.out::println);
        System.out.println();
        spec.forEach(System.out::println);
        //System.out.println(response.toString());

    }

    private static JSONObject getJSONRequest(String url) throws IOException {

        URL obj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return new JSONObject(response.toString());
    }

    private static Integer getSalary(Object oSalary) {
        if (oSalary.equals(JSONObject.NULL)) return null;
        JSONObject salary = (JSONObject) oSalary;
        Integer from = returnInt(salary.get("from"));
        Integer to = returnInt(salary.get("to"));
        switch (salary.getString("currency")) {
            case "RUR":
                return getSalary(from, to, 1.0);
            case "AZN":
                return getSalary(from, to, 0.024719);
            case "BYR":
                return getSalary(from, to, 0.034692);
            case "EUR":
                return getSalary(from, to, 0.012826);
            case "GEL":
                return getSalary(from, to, 0.0344);
            case "KGS":
                return getSalary(from, to, 1.077156);
            case "KZT":
                return getSalary(from, to, 5.835565);
            case "UAH":
                return getSalary(from, to, 0.387637);
            case "USD":
                return getSalary(from, to, 0.01457);
            case "UZS":
                return getSalary(from, to, 147.875838);
            default:
                throw new IllegalStateException("Unexpected value: " + salary.getString("currency"));
        }
    }

    private static Integer returnInt(Object object) {
        if (object.equals(JSONObject.NULL)) return null;
        return (Integer) object;
    }

    private static Integer getSalary(Integer from, Integer to, double v) {
        if (from == null) {
            return to;
        } else {
            if (to == null) {
                return from;
            } else {
                double res = (from + to) / v / 2;
                return (int) res;
            }
        }
    }
}