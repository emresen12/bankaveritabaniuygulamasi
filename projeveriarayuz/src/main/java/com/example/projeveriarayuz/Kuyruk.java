package com.example.projeveriarayuz;

public class Kuyruk {
    Node front;
    Node rear;

    public Kuyruk() {
        this.front = this.rear = null;
    }

    public void enqueue(Node yeniNode) {
        if (rear == null) {
            front = rear = yeniNode;
        } else {
            rear.next = yeniNode;
            rear = yeniNode;
        }
    }

    public Node dequeue() {
        if (front == null)
            return null;

        Node temp = front;
        front = front.next;

        if (front == null)
            rear = null;

        return temp;
    }

    public boolean isEmpty() {
        return front == null;
    }
}
