package com.example.perfectphotoapp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.example.perfectphotoapp.Face.compareFaces;
import static com.example.perfectphotoapp.Face.matchFaces;
import static org.junit.Assert.*;

public class FaceTest {
    Face f1 = new Face(10,10,30,30);
    Face f2 = new Face(15,15,25,25);
    Face f3 = new Face(22,16,28,26);
    Face f4 = new Face(25,25,45,45);
    Face f5 = new Face(33,20,50,40);

    @Test
    public void centerTest() {
        assertTrue(f1.centerTest(f2));
        assertTrue(f2.centerTest(f1));

        assertFalse(f1.centerTest(f3));
        assertFalse(f3.centerTest(f1));

        assertFalse(f2.centerTest(f3));
        assertFalse(f3.centerTest(f2));

        assertFalse(f1.centerTest(f4));
        assertFalse(f4.centerTest(f1));

        assertFalse(f1.centerTest(f5));
        assertFalse(f5.centerTest(f1));

        assertFalse(f3.centerTest(f4));
        assertFalse(f4.centerTest(f3));

        assertTrue(f4.centerTest(f5));
        assertTrue(f5.centerTest(f4));
    }

    @Test
    public void matchFacesTest() {
        List<Face> faceList = new ArrayList<>();
        faceList.add(f1);
        faceList.add(f2);
        faceList.add(f4);

        List<Integer> answers = new ArrayList<>();

        assertTrue(answers.equals(matchFaces(f3, faceList)));

        answers.add(2);

        assertTrue(answers.equals(matchFaces(f4, faceList)));
        assertTrue(answers.equals(matchFaces(f5, faceList)));

        answers.clear();
        answers.add(0);
        answers.add(1);

        assertTrue(answers.equals(matchFaces(f1, faceList)));
        assertTrue(answers.equals(matchFaces(f2, faceList)));
    }

    @Test
    public void compareFacesTest() {
        Face[] list1 = {};
        Face[] list2 = {};

        assertArrayEquals(list1, compareFaces(list1, list2, 3));

        list2 = new Face[] {f1};

        assertArrayEquals(list2, compareFaces(list1, list2, 3));
        assertArrayEquals(list2, compareFaces(list2, list1, 3));

        list2 = new Face[] {f1, f2, f3};

        assertArrayEquals(list2, compareFaces(list1, list2, 3));
        assertArrayEquals(new Face[] {f1, f3}, compareFaces(list2, list1, 3));

        list1 = new Face[] {f2};

        assertArrayEquals(list2, compareFaces(list1, list2, 3));
        assertArrayEquals(new Face[] {f2, f3}, compareFaces(list2, list1, 3));

        list2 = new Face[] {f1, f3, f4};
        list1 = new Face[] {f2, f4, f5};

        assertArrayEquals(list2, compareFaces(list1, list2, 3));
        assertArrayEquals(new Face[] {f2, f4, f5, f3}, compareFaces(list2, list1, 3));

        f1.age = 3;
        f2.age = 3;
        f3.age = 3;
        f4.age = 2;

        list1 = new Face[] {f1};
        list2 = new Face[] {f2, f3, f4};

        assertArrayEquals(new Face[] {f1, f4}, compareFaces(list2, list1, 3));
    }
}