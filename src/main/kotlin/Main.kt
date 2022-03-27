import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import java.rmi.server.ServerNotActiveException
import java.util.*
//import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private const val LATENCY = 700L
private const val RESPONSE_LENGTH = 2048

fun main() {
    // Функции можно вызывать отсюда для проверки
    //  для ДЗ лучше использовать blockingSubscribe вместо subscribe потому что subscribe подпишется на изменения,
    //  но изменения в большинстве случаев будут получены позже, чем выполнится функция main, поэтому в консоли ничего
    //  не будет выведено. blockingSubscribe работает синхронно, поэтому результат будет выведен в консоль
    //
    //  В реальных программах нужно использовать subscribe или передавать данные от источника к источнику для
    //  асинхронного выполнения кода.
    //
    //  Несмотря на то, что в некоторых заданиях фигурируют слова "синхронный" и "асинхронный" в рамках текущего ДЗ
    //  это всего лишь имитация, реальное переключение между потоками будет рассмотрено на следующем семинаре

    println("=== Start ===")

    println("1. requestDataFromServerAsync() : Single ")
    requestDataFromServerAsync()
        .blockingSubscribe(
            {
                println("success")
            },
            {
                println("complete")
            }
        )


    println("2. requestServerAsync(): Completable ")
    requestServerAsync()
        .blockingSubscribe(
            {
                println("success")
            },
            {
                println("complete")
            }
        )

    println("3. requestDataFromDbAsync : Maybe ")
    requestDataFromDbAsync<Int>()
        .blockingSubscribe(
            {
                println("success")
                println("$it")
            },
            {
                println("error")
                println("${it.message}")
            },
            {
                println("complete")
            }
        )
    println("4. emitEachSecond() ")
    emitEachSecond()



//  xMap { flatMapCompletable(it) }
//  xMap { concatMapCompletable (it) }
//  xMap { switchMapCompletable(it) }

    println("5. xMap flatMapCompletable ")

    xMap { Flowable.range(1,5).flatMapCompletable(it) }
//    flatMap создает одну цепочку, и выполняет на элементах исходного Flowable
//    заданную функцию (тоже Flowable), не сохраняя порядок элементов. Итоговый Flowable
//    содержит преобразованные элементы в случайной последовательности.
//   s = Lists.newArrayList("a", "b", "c", "d", "e", "f");
//   flatMap( s -> {
//                return Observable.just(s + "x")
//   Lets run this test to see its result: [cx, ex, fx, bx, dx, ax]

    println("5. xMap concatMapCompletable ")
//    concatMap создает одну цепочку, и выполняет на элементах исходного Flowable
//    заданную функцию (тоже Flowable), сохраняя порядок элементов. Итоговый Flowable
//    содержит преобразованные элементы в соответствующей последовательности.
//    result: [ax, bx, cx, dx, ex, fx].

    xMap { Flowable.range(1,5).concatMapCompletable(it) }
// switchMap в отличие от flatMap будет отписываться от прошлых поисков при запуске нового

    println("5. xMap switchMapCompletable ")

//    Result: [fx]

    xMap { Flowable.range(1,5).switchMapCompletable(it) }


    println("=== Finish ===")

}


//For RxJava it will be:
//private fun readCacheRx(data: String? = null): Maybe<String> {
//    return if (data != null) {
//        Maybe
//                .just(data)
//                .delay(100, TimeUnit.MILLISECONDS)
//                .doOnSuccess { println("read from cache: $data") }
//    } else {
//        Maybe
//                .empty<String>()
//                .delay(100, TimeUnit.MILLISECONDS)
//                .doOnComplete { println("read from cache: $data") }
//    }
//}
//private fun readNetworkRx(data: String = "data"): Single<String> {
//    return Single
//            .just(data)
//            .delay(100, TimeUnit.MILLISECONDS)
//            .doOnSuccess { println("read from network: $data") }
//}
//private fun saveCacheRx(data: String): Completable {
//    return Completable
//            .fromAction {
//                println("saved to cache: $data")
//            }
//            .delay(100, TimeUnit.MILLISECONDS)
//}

// 1) Какой источник лучше всего подойдёт для запроса на сервер, который возвращает результат?
// Почему?
// Дописать функцию
// Вы на него подписываетесь и получаете либо элемент, либо ошибку. Но при этом Single является реактивным.


fun requestDataFromServerAsync() : Single<ByteArray> /* -> ???<ByteArray> */ {

    // Функция имитирует синхронный запрос на сервер, возвращающий результат
    fun getDataFromServerSync(): ByteArray? {
        Thread.sleep(LATENCY)
        val success = Random.nextBoolean()
        return if (success) Random.nextBytes(RESPONSE_LENGTH) else null
    }

    /* return ??? */
    return Single
        .fromCallable { getDataFromServerSync() }
}


// 2) Какой источник лучше всего подойдёт для запроса на сервер, который НЕ возвращает результат?
// Почему?
// Дописать функцию
// Completable - Он похож на void-метод. Он либо успешно завершает свою работу 
// без каких-либо данных, либо бросает исключение. То есть это некий кусок кода, 
// который можно запустить, и он либо успешно выполнится, либо завершится сбоем.
fun requestServerAsync(): Completable /* -> Completable */ {

    // Функция имитирует синхронный запрос на сервер, не возвращающий результат
    fun getDataFromServerSync() {
        Thread.sleep(LATENCY)
        if (Random.nextBoolean()) throw ServerNotActiveException()
    }

    /* return ??? */
    return Completable
            .fromCallable { getDataFromServerSync() }
}

// 3) Какой источник лучше всего подойдёт для однократного асинхронного возвращения значения из базы данных?
// Почему?
// Дописать функцию
// Maybe. Этот тип, который может либо содержать элемент, либо выдать ошибку, либо не содержать данных


fun <T> requestDataFromDbAsync() : Maybe <T>/* -> Single */ {

    // Функция имитирует синхронный запрос к БД не возвращающий результата
    fun getDataFromDbSync(): T? {
        Thread.sleep(LATENCY); return null
    }

    /* return */
    return Maybe.fromCallable { getDataFromDbSync() }
}

// 4) Примените к источнику оператор (несколько операторов), которые приведут к тому, чтобы элемент из источника
// отправлялся раз в секунду (оригинальный источник делает это в 2 раза чаще).
// Значения должны идти последовательно (0, 1, 2 ...)
// Для проверки результата можно использовать .blockingSubscribe(::printer)
fun emitEachSecond() {

    // Источник
    fun source(): Flowable<Long> = Flowable.interval(500, TimeUnit.MILLISECONDS)

    // Принтер
    fun printer(value: Long) = println("${Date()}: value = $value")

    // code here
    source()
        .sample(1, TimeUnit.SECONDS)
        .map { it/2 }
        .blockingSubscribe (  ::printer)
}



// 5) Функция для изучения разницы между операторами concatMap, flatMap, switchMap
// Нужно вызвать их последовательно и разобраться чем они отличаются
// Документацию в IDEA можно вызвать встав на функцию (например switchMap) курсором и нажав hotkey для вашей раскладки
// Mac: Intellij Idea -> Preferences -> Keymap -> Быстрый поиск "Quick documentation"
// Win, Linux: File -> Settings -> Keymap -> Быстрый поиск "Quick documentation"
//
// конструкция в аргументах функции xMap не имеет значения для ДЗ и создана для удобства вызова функции, чтобы была
//  возможность удобно заменять тип маппинга
//
// Вызов осуществлять поочерёдно из функции main
//
//  xMap { flatMapCompletable(it) }
//  xMap { concatMapCompletable (it) }
//  xMap { switchMapCompletable(it) }
//
fun xMap(mapper: Flowable<Int>.(internalMapper: (Int) -> Completable) -> Completable) {

    fun waitOneSecond() = Completable.timer(1, TimeUnit.SECONDS)

    println("${Date()}: start")
    Flowable.fromIterable(0..20)
        .mapper { iterableIndex ->

            waitOneSecond()
                .doOnComplete { println("${Date()}: finished operation for iterable index $iterableIndex") }

        }
        .blockingSubscribe()
}