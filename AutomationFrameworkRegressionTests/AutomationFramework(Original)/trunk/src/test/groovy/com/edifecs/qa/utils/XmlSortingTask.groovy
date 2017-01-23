package com.edifecs.qa.utils;

import groovy.xml.XmlUtil

/**
 * @author alexandruz
 * @version 1.0
 */
class XmlSortingTask {

  private File source;
  private File destination;
  private String sourceString
  private String destinationString
  private Comparator<Node> comparator;

//groovy 1.6 doesn't work
//  static final Comparator<Node> DEFAULT_COMPARATOR = new Comparator<Node>() {
//    int compare(Node o1, Node o2) {
//      def value1 = "$o1${o1.children()}"
//      def value2 = "$o2${o2.children()}"
//      value1 <=> value2
//    }
//  }

  static final def compareClosure = {
    Node o1, Node o2 ->
    def value1 = "$o1${o1.children()}"
    def value2 = "$o2${o2.children()}"
    value1 <=> value2
  }

  static final Comparator<Node> DEFAULT_COMPARATOR = compareClosure as Comparator<Node>

  def XmlSortingTask(File source,
                     File destination = null, Comparator<Node> comparator = null) {
    this.source = source;
    this.destination = !destination ? source : destination;
    this.comparator = !comparator ? DEFAULT_COMPARATOR : comparator;
  }

  def XmlSortingTask(String source, Comparator<Node> comparator = null) {
    this.sourceString = source;
    this.comparator = !comparator ? DEFAULT_COMPARATOR : comparator;
  }

  def void execute() {
    def nodes = new XmlParser().parse(source)
    nodes.breadthFirst().reverse().each {Node node ->
      def sortedAttributes = new TreeMap(node.attributes())
      node.attributes().clear()
      node.attributes().putAll(sortedAttributes)
      node.children().sort(comparator)
    }
    destination.text = XmlUtil.serialize(nodes)
  }

  def executeOnStrings() {
    def nodes = new XmlParser().parseText(sourceString)
    nodes.breadthFirst().reverse().each {Node node ->
      def sortedAttributes = new TreeMap(node.attributes())
      node.attributes().clear()
      node.attributes().putAll(sortedAttributes)
      node.children().sort(comparator)
    }

    destinationString = XmlUtil.serialize(nodes)

  }

/*    public static void main(String[] args) {
        new XmlSortingTask(new File("D:/1srt/1Sample.xml"), new File("D:/1srt/1Sample_sorted.xml")).execute();
        new XmlSortingTask(new File("D:/1srt/2Sample.xml"), new File("D:/1srt/2Sample_sorted.xml")).execute();
    }*/
}
