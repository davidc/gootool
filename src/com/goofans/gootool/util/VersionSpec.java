/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.util;

import java.util.StringTokenizer;
import java.util.Arrays;

/**
 * Specification of a version in n.n.n.n format (up to 4 numbers).
 * Immutable after construction.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class VersionSpec implements Comparable<VersionSpec>
{
  private static final int MAX_FIELDS = 4;

  private final int[] version = new int[MAX_FIELDS];
  private int numDisplayFields;

  public VersionSpec(int major, int minor, int micro)
  {
    version[0] = major;
    version[1] = minor;
    version[2] = micro;
    this.numDisplayFields = 3;
  }

  public VersionSpec(int[] inVersion)
  {
    if (inVersion.length < 1 || inVersion.length > 4) throw new NumberFormatException("Version has too many/too few fields");
    System.arraycopy(inVersion, 0, version, 0, inVersion.length);
    this.numDisplayFields = inVersion.length;
  }

  public VersionSpec(String versionStr) throws NumberFormatException
  {
    StringTokenizer tok = new StringTokenizer(versionStr, ".");
    numDisplayFields = 0;

    if (!tok.hasMoreTokens()) throw new NumberFormatException("No version given");

    while (tok.hasMoreTokens()) {
      if (numDisplayFields == MAX_FIELDS) throw new NumberFormatException("Version has too many fields");
      int field = Integer.parseInt(tok.nextToken());
      version[numDisplayFields++] = field;
    }
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < numDisplayFields; ++i) {
      if (i != 0) sb.append('.');
      sb.append(version[i]);
    }
    return sb.toString();
  }

  public int compareTo(VersionSpec that)
  {
    // Compare all components 0-3. Since unused fields default to 0, this allows correct comparison of 1.1 and 1.1.5.
    for (int i = 0; i < MAX_FIELDS; ++i) {
      if (this.version[i] < that.version[i]) return -1;
      if (this.version[i] > that.version[i]) return +1;
    }

    return 0;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VersionSpec that = (VersionSpec) o;

    return (compareTo(that) == 0);
  }

  @Override
  public int hashCode()
  {
    return Arrays.hashCode(version);
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public static void main(String[] args)
  {
    testCreation("1", new int[]{1, 0, 0, 0});
    testCreation("1.2", new int[]{1, 2, 0, 0});
    testCreation("1.2.3", new int[]{1, 2, 3, 0});
    testCreation("1.2.3.0", new int[]{1, 2, 3, 0});
    testCreation("1.2.003", new int[]{1, 2, 3, 0});
    testCreation("00001.0002.0003", new int[]{1, 2, 3, 0});
    testCreation("1.2.3.4", new int[]{1, 2, 3, 4});

    testCreationThatShouldFail("1.2.3.4.5");
    testCreationThatShouldFail("1.2.3.4.5.6.7.8.9");
    testCreationThatShouldFail("1A");
    testCreationThatShouldFail("a0.1.2.3");
    testCreationThatShouldFail("1 .2 .3");
    testCreationThatShouldFail("");

    testComparison("1.2.3", "1.2", 1);
    testComparison("1.2", "1.2.3", -1);
    testComparison("1.2.0", "1.2", 0);

    testComparison("11.2", "5.2", 1);

    testComparison("1.1.1", "1.1.2", -1);
    testComparison("1.1.2", "1.1.1", 1);

    testComparison(new VersionSpec(1, 2, 3), new VersionSpec("1.2.3"), 0);

    testEquality("1", "1.0.0.0", true);
    testEquality("1.0", "1", true);
    testEquality("1", "1.0.0.1", false);
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  private static void testCreation(String s, int[] expected)
  {
    VersionSpec v = new VersionSpec(s);
    System.out.println("creation: " + v);

    if (!Arrays.equals(v.version, expected)) throw new RuntimeException("creation test failed");
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  private static void testCreationThatShouldFail(String s)
  {
    try {
      testCreation(s, new int[]{0, 0, 0, 0});
      throw new RuntimeException("Didn't get expected exception!");
    }
    catch (NumberFormatException e) {
      System.out.println("creation: got expected exception: " + e);
    }
  }

  private static void testComparison(String s1, String s2, int expected)
  {
    VersionSpec v1 = new VersionSpec(s1);
    VersionSpec v2 = new VersionSpec(s2);

    testComparison(v1, v2, expected);
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  private static void testComparison(VersionSpec v1, VersionSpec v2, int expected)
  {
    int result = v1.compareTo(v2);

    char resultCh;
    if (result < 0) resultCh = '<';
    else if (result > 0) resultCh = '>';
    else resultCh = '=';

    System.out.println("comparison: " + v1 + " " + resultCh + " " + v2);

    if (result != expected) throw new RuntimeException("comparison test failed");
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  private static void testEquality(String s1, String s2, boolean expectEquality)
  {
    VersionSpec v1 = new VersionSpec(s1);
    VersionSpec v2 = new VersionSpec(s2);

    boolean equals = v1.equals(v2);
    System.out.println("equality: " + v1 + " " + (equals ? "equals" : "doesn't equal") + " " + v2);

    if (expectEquality != equals) throw new RuntimeException("equality test failed");
  }
}
