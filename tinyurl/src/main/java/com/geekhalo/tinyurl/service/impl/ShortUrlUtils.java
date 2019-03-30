package com.geekhalo.tinyurl.service.impl;

import com.geekhalo.tinyurl.service.NumberEncoder;
import com.geekhalo.tinyurl.service.NumberEncoder;

import java.util.Stack;

/**
 * @description :短链工具
 * @author: liuxiaosong
 * @date: 2018-07-23 11:48
 **/
public class ShortUrlUtils implements NumberEncoder {
    private static final char[] letters = {'q', 'w', 'e', 'r', 't', 'y', 'u',
            'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z',
            'x', 'c', 'v', 'b', 'n', 'm', '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P',
            'A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L', 'Z', 'X', 'C', 'V',
            'B', 'N', 'M'};

    private static String getShortDomain(long number) {
        Long rest = number;
        Stack<Character> stack = new Stack<>();
        StringBuilder result = new StringBuilder(0);
        while (rest != 0) {

            stack.add(ShortUrlUtils.letters[new Long((rest - (rest / 62) * 62)).intValue()]);
            rest = rest / 62;
        }
        for (; !stack.isEmpty(); ) {
            result.append(stack.pop());
        }

        return result.toString();

    }

    private static Long getId(String sixty_str) {
        int multiple = 1;
        long result = 0;
        Character c;
        for (int i = 0; i < sixty_str.length(); i++) {
            c = sixty_str.charAt(sixty_str.length() - i - 1);
            result += convertToSixTwo(c) * multiple;
            multiple = multiple * 62;
        }
        return result;
    }

    private static int convertToSixTwo(Character c) {
        for (int i = 0; i < ShortUrlUtils.letters.length; i++) {
            if (c == ShortUrlUtils.letters[i]) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String encode(Long id) {
        return getShortDomain(id);
    }

    @Override
    public Long decode(String str) {
        return getId(str);
    }
}
