import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CacheTest {

	public static void main(String[] args) {
		final Cache cache = new Cache();
		ExecutorService threadPool = Executors.newCachedThreadPool();
		for (int i = 0; i < 10; i++) {
			threadPool.execute(new Runnable() {

				@Override
				public void run() {
					Object object = cache.get("key");
					System.out.println(object);
				}
			});
		}
		threadPool.shutdown();
	}
}

class Cache {
	private Map<String, Object> map = new HashMap<>();
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private Object value = null;

	public Object get(String key) {
		lock.readLock().lock();
		try {
			value = map.get(key);
			if (value == null) {
				lock.readLock().unlock();
				lock.writeLock().lock();
				try {
					if (value == null) {
						value = new Random().nextInt(1000);
						map.put(key, value);
					}
				} finally {
					lock.writeLock().unlock();
				}
				lock.readLock().lock();
			}
		} finally {
			lock.readLock().unlock();
		}
		return value;
	}
}
