package com.example.perfectphotoapp;

import org.junit.Test;

import static org.junit.Assert.*;

public class FaceTest {

    @Test
    public void centerTest() {
        Face f1 = new Face(10,10,30,30);
        Face f2 = new Face(15,15,25,25);
        Face f3 = new Face(22,16,28,26);
        Face f4 = new Face(25,25,45,45);
        Face f5 = new Face(33,20,50,40);

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
}