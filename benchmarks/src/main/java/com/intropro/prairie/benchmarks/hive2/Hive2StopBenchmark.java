package com.intropro.prairie.benchmarks.hive2;

import com.intropro.prairie.unit.common.DependencyResolver;
import com.intropro.prairie.unit.common.exception.PrairieException;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Created by presidentio on 10/27/15.
 */
@Fork(3)
@Warmup(iterations = 0)
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 3)
@BenchmarkMode(Mode.SampleTime)
@State(Scope.Thread)
@Threads(1)
public class Hive2StopBenchmark {

    private Hive2UnitContainer hive2UnitContainer;
    private DependencyResolver dependencyResolver;

    @Setup(Level.Invocation)
    public void init() throws PrairieException {
        hive2UnitContainer = new Hive2UnitContainer();
        dependencyResolver = new DependencyResolver();
        dependencyResolver.resolve(hive2UnitContainer);
    }

    @Benchmark
    public void measureStop() throws PrairieException {
        dependencyResolver.destroy(hive2UnitContainer);
    }

}
