//package com.instanza.i18nmanager;
//
//import org.assertj.core.util.Lists;
//
//import java.util.*;
//import java.util.function.*;
//import java.util.stream.Stream;
//
///**
// * Created by luanhaipeng on 16/12/19.
// */
//public class Test {
//
//
//    public static void main(String args[]){
//
//        Map<String,Object> map = new HashMap<>();
//
//        map.put("a","aaaa");
//
//        map.compute("a",(String a, Object b)->{
//
//
//
//            return "11111";
//        });
//
//
//        System.out.println(map);
//
//
//        int aaa [] = new int[]{1,2,3};
//
//        Map<String,Object> map2= new HashMap(){
//            {
//                put("Name", "Unmi");
//                put("QQ", "1125535");
//            }
//        };
//
//
//        List<Integer> nums = Lists.newArrayList(1, null, 3, 40, null, 6,11,32);
//
//        Stream<Integer> stream0 = nums.stream().map((b) -> {
//            if (b == null) {
//                return 0;
//            }
//            return b;
//        }).filter((b) -> {
//            return b > 10;
//        });
//
//
//        long count = stream0.count();
//
//        System.out.println(count);
//
//
//
//
//        Function<Integer,Long> function = (a)->{
//            System.out.println(a + count);
//            return a+count;
//        };
//
//        Long result = function.apply(1000);
//
//        System.out.println(result);
//
//
//
//
//    }
//}
