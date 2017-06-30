/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.dataflow;

import com.facebook.litho.testing.testrunner.ComponentsTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.facebook.litho.dataflow.GraphBinding.create;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(ComponentsTestRunner.class)
public class DataFlowGraphTest {

  private UnitTestTimingSource mTestTimingSource;
  private DataFlowGraph mDataFlowGraph;

  @Before
  public void setUp() throws Exception {
    mTestTimingSource = new UnitTestTimingSource();
    mDataFlowGraph = DataFlowGraph.create(mTestTimingSource);
  }

  @Test
  public void testSimpleGraph() {
    SettableNode source = new SettableNode();
    SimpleNode middle = new SimpleNode();
    OutputOnlyNode destination = new OutputOnlyNode();

    GraphBinding binding = create(mDataFlowGraph);
    binding.addBinding(source, middle);
    binding.addBinding(middle, destination);
    binding.activate();

    mTestTimingSource.step(1);

    assertThat(destination.getValue()).isEqualTo(0f);
    assertThat(source.getValue()).isEqualTo(0f);

    source.setValue(37);
    mTestTimingSource.step(1);

    assertThat(destination.getValue()).isEqualTo(37f);
    assertThat(source.getValue()).isEqualTo(37f);
  }

  @Test
  public void testSimpleUpdatingGraph() {
    NumFramesNode source = new NumFramesNode();
    SimpleNode middle = new SimpleNode();
    OutputOnlyNode destination = new OutputOnlyNode();

    GraphBinding binding = create(mDataFlowGraph);
    binding.addBinding(source, middle);
    binding.addBinding(middle, destination);
    binding.activate();

    mTestTimingSource.step(1);

    assertThat(destination.getValue()).isEqualTo(1f);
    assertThat(source.getValue()).isEqualTo(1f);

    mTestTimingSource.step(39);

    assertThat(destination.getValue()).isEqualTo(40f);
    assertThat(source.getValue()).isEqualTo(40f);

    mTestTimingSource.step(1);

    assertThat(destination.getValue()).isEqualTo(41f);
    assertThat(source.getValue()).isEqualTo(41f);
  }

  @Test
  public void testGraphWithMultipleOutputs() {
    NumFramesNode source = new NumFramesNode();
    SimpleNode middle = new SimpleNode();
    OutputOnlyNode dest1 = new OutputOnlyNode();
    OutputOnlyNode dest2 = new OutputOnlyNode();
    OutputOnlyNode dest3 = new OutputOnlyNode();

    GraphBinding binding = create(mDataFlowGraph);
    binding.addBinding(source, middle);
    binding.addBinding(middle, dest1);
    binding.addBinding(middle, dest2);
    binding.addBinding(source, dest3);
    binding.activate();

    mTestTimingSource.step(1);

    assertThat(dest1.getValue()).isEqualTo(1f);
    assertThat(dest2.getValue()).isEqualTo(1f);
    assertThat(dest3.getValue()).isEqualTo(1f);

    mTestTimingSource.step(39);

    assertThat(dest1.getValue()).isEqualTo(40f);
    assertThat(dest2.getValue()).isEqualTo(40f);
    assertThat(dest3.getValue()).isEqualTo(40f);
  }

  @Test
  public void testRebindNode() {
    SettableNode source = new SettableNode();
    SimpleNode middle = new SimpleNode();
    OutputOnlyNode destination = new OutputOnlyNode();

    GraphBinding binding = create(mDataFlowGraph);
    binding.addBinding(source, middle);
    binding.addBinding(middle, destination);
    binding.activate();

    mTestTimingSource.step(1);

    assertThat(destination.getValue()).isEqualTo(0f);

    SettableNode newSource = new SettableNode();
    GraphBinding secondBinding = create(mDataFlowGraph);
    secondBinding.addBinding(newSource, destination);
    secondBinding.activate();

    mTestTimingSource.step(1);

    assertThat(destination.getValue()).isEqualTo(0f);

    newSource.setValue(11);
    mTestTimingSource.step(1);

    assertThat(destination.getValue()).isEqualTo(11f);
  }

  @Test
  public void testMultipleInputs() {
    AdditionNode dest = new AdditionNode();
    SettableNode a = new SettableNode();
    SettableNode b = new SettableNode();
    a.setValue(1776);
    b.setValue(1812);

    GraphBinding binding = create(mDataFlowGraph);
    binding.addBinding(a, dest, "a");
    binding.addBinding(b, dest, "b");
    binding.activate();

    mTestTimingSource.step(1);

    assertThat(dest.getValue()).isEqualTo(3588f);
  }

  @Test(expected = DetectedCycleException.class)
  public void testSimpleCycle() {
    SimpleNode node1 = new SimpleNode();
    SimpleNode node2 = new SimpleNode();
    SimpleNode node3 = new SimpleNode();
    SimpleNode node4 = new SimpleNode();

    GraphBinding binding = GraphBinding.create(mDataFlowGraph);
    binding.addBinding(node1, node2);
    binding.addBinding(node2, node3);
    binding.addBinding(node3, node1);
    binding.addBinding(node1, node4);
    binding.activate();

    mTestTimingSource.step(1);
  }

  @Test(expected = DetectedCycleException.class)
  public void testCycleWithoutLeaves() {
    SimpleNode node1 = new SimpleNode();
    SimpleNode node2 = new SimpleNode();
    SimpleNode node3 = new SimpleNode();

    GraphBinding binding = GraphBinding.create(mDataFlowGraph);
    binding.addBinding(node1, node2);
    binding.addBinding(node2, node3);
    binding.addBinding(node3, node1);
    binding.activate();

    mTestTimingSource.step(1);
  }
}
