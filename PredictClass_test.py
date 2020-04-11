# This file conducts unit tests on the mostFrequent
# method from the PredictClass module. 2 lists are
# provided as parameters for the method. 4 tests are
# conducted. Test 1 and 3 are expected to fail, while
# Test 2 and 4 are expected to succeed.

import unittest
from PredictClass import mostFrequent

# Create a list of different integers
list1 = [1, 1, 1, 1, 1, 1, 2]

# Create a list of different instrument classes
list2 = ["Acoustic Guitar", "Acoustic Guitar", "Acoustic Guitar", "Acoustic Guitar", "Flute"]


# Test 1 - Pass in list1, expected result is that 1 is returned
class TestMostFrequent1(unittest.TestCase):
    def test_mostFrequent1(self):
        self.assertEqual(mostFrequent(list1), 2)


# Test 2 - Pass in list1, expected result is that 1 is returned
class TestMostFrequent2(unittest.TestCase):
    def test_mostFrequent2(self):
        self.assertEqual(mostFrequent(list1), 1)


# Test 3 - Pass in list2 - expected result is that "Acoustic Guitar" is returned
class TestMostFrequent3(unittest.TestCase):
    def test_mostFrequent3(self):
        self.assertEqual(mostFrequent(list2), "Flute")


# Test 4 - Pass in list2 - expected result is that "Acoustic Guitar" is returned
class TestMostFrequent4(unittest.TestCase):
    def test_mostFrequent4(self):
        self.assertEqual(mostFrequent(list2), "Acoustic Guitar")