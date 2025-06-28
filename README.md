
# 📦 Sprint 1: Naive implementation


> > **"Scheduler must be safe to use across multiple threads."**

---

## ✅ What This Means

The **JobScheduler** component must allow multiple threads to:

* Submit jobs at the same time (e.g., user threads, REST handlers, CLI, etc.)
* Submit both immediate and delayed jobs
* Do so **without corrupting internal state**, losing jobs, or causing race conditions

---

## ✅ Possible Solutions

Here are the main strategies you can use to make `JobScheduler` thread-safe:

---

### 🔒 1. **Internal Synchronization**

* Use `synchronized` blocks or methods to protect critical sections (e.g., appending to job queue, triggering executor).
* Simple but coarse-grained and may hurt throughput under high contention.

✅ Good when: simplicity is preferred, contention is low
⚠️ Not ideal for high-concurrency use cases

---

### 🧱 2. **Use of Concurrent Data Structures**

* Back the internal job storage/queue with:

    * `ConcurrentLinkedQueue`
    * `LinkedBlockingQueue`
    * `DelayQueue` (for delayed jobs)
* Allows multiple threads to enqueue jobs safely without manual locks.

✅ Best for producer-consumer or stream-oriented designs
🔄 Often paired with a background dispatcher thread

---

### 🔐 3. **Lock-based Fine-Grained Control**

* Use a `ReentrantLock` to guard only shared state updates (e.g., atomic counters, state transitions, dispatch buffers).
* Use `Condition` to notify workers when new jobs are available (optional).

✅ Gives better performance under moderate to high concurrency
⚠️ You must be careful about deadlocks, fairness, missed signals

---

### 🔄 4. **Thread-safe Delegation**

* If `JobScheduler` just delegates to an underlying executor (`JobExecutor`), and that executor is thread-safe, you may **only need to ensure the delegation logic is safe**.
* This can be done using:

    * Immutable design (e.g., `final` references)
    * Local variables only (stateless delegation)
    * Thread-safe constructor/init

✅ Easy and clean if you delegate all heavy lifting

---

### 🧪 5. **Idempotent & Stateless Submission**

* Design `submit()` and `submitDelayed()` to be:

    * Pure: no shared mutable state
    * Stateless: each call builds its own `Runnable` and pushes it to a safe queue or executor

✅ Often gives you safety *for free*
⚠️ You still need to ensure that all underlying components (queues, pools) are thread-safe

---

## 🧠 Real-World Best Practice

In production-grade schedulers:

* Job submission uses **lock-free or concurrent queues**
* Delayed jobs use `DelayQueue` (a `BlockingQueue` with scheduled execution ordering)
* A **dispatcher thread** pulls from the delay queue and forwards to a `ThreadPoolExecutor`
* All submission logic is **stateless** or **delegates to safe structures**


# 📦 Sprint 2: Job Scheduler with Dispatcher & Delayed Execution

## 🎯 Goal

Upgrade the basic scheduler to a **production-grade** design with:

* Proper **delayed job scheduling**
* **Central dispatcher loop**
* **Thread-safe queueing**
* Preserved **max concurrency control**

---

## ✅ Features Implemented

| Feature                            | Description                                                 |
| ---------------------------------- | ----------------------------------------------------------- |
| **BlockingQueue-based scheduling** | Uses `DelayQueue` to manage both immediate and delayed jobs |
| **Centralized Dispatcher Thread**  | One background thread pulls jobs and submits to executor    |
| **Semaphore Enforcement**          | Max concurrency limit enforced at execution                 |
| **Graceful Shutdown**              | Dispatcher and executor can be stopped cleanly              |
| **Thread-safe Submission**         | Multiple threads can submit jobs concurrently               |

---

## ⚙️ Components

### 1. `JobScheduler`

* Exposes `.submit()` and `.submitDelayed()`
* Internally adds jobs into `DelayQueue<ScheduledJob>`
* Runs `dispatchLoop()` on a single dedicated thread

### 2. `JobExecutor`

* Wraps a cached thread pool
* Uses `Semaphore` to limit concurrent executions

### 3. `ScheduledJob`

* Implements `Delayed` to sort jobs by scheduled execution time
* Compatible with `DelayQueue`

---

## 🛡 Design Principles

| Principle                  | How it's Achieved                                                               |
| -------------------------- | ------------------------------------------------------------------------------- |
| **Avoid thread leaks**     | No `new Thread().sleep()`; all jobs go through a pooled dispatcher              |
| **Time-aware scheduling**  | Uses `DelayQueue` for automatic delay handling                                  |
| **Separation of concerns** | Scheduler queues jobs, executor manages concurrency                             |
| **Scalability**            | Can handle bursty submissions safely without spinning or blocking unnecessarily |
