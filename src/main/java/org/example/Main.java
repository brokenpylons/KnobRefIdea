package org.example;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

interface Knob {
    Knob withValue(int number);
    int getValue();
}

class BooleanKnob implements Knob {
    public BooleanKnob(boolean value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value ? 1 : 0;
    }

    @Override
    public BooleanKnob withValue(int number) {
         return new BooleanKnob(switch (number) {
            case 0 -> false;
            case 1 -> true;
            default -> throw  new RuntimeException();
        });
    }

    public boolean get() {
        return value;
    }

    private final boolean value;
}

record Config(BooleanKnob toggle1, BooleanKnob toggle2) {

    // poor man's reflection (actually the fastest)
    public Config(Knob[] args) {
        this((BooleanKnob) args[0], (BooleanKnob) args[1]);
    }

    public Knob[] knobs() {
        return new Knob[]{toggle1, toggle2};
    }
}

class KnobsRef<T> {
    private final Class<T> cls;
    private final Constructor<T> ctor;

    public KnobsRef(Class<T> cls) {
        this.cls = cls;

        Class<?>[] componentTypes = Arrays.stream(cls.getRecordComponents())
                .map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
        if (!Arrays.stream(componentTypes)
                .map(Knob.class::isAssignableFrom)
                .reduce(true, (x, y) -> x && y)) {
            throw new RuntimeException();
        }
        try {
            this.ctor = cls.getDeclaredConstructor(componentTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    public T construct(Object[] args) {
        try {
            return (T) ctor.newInstance(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Knob[] extract(T obj) {
        return Arrays.stream(cls.getRecordComponents())
                .map(t -> {
                    try {
                        return (Knob)t.getAccessor().invoke(obj);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(Knob[]::new);
    }
}

public class Main {

    private static Knob[] mutate(Knob[] knobs) {
        return Arrays.stream(knobs)
                .map(k -> k.withValue((k.getValue() + 1) % 2))
                .toArray(Knob[]::new);
    }

    private static void eval(Config config) {
        System.out.println(config.toggle1().get());
        System.out.println(config.toggle2().get());
    }

    public static void main(String[] args) {
        var ref = new KnobsRef<>(Config.class);

        var config = new Config(new BooleanKnob(true), new BooleanKnob(false));
        Knob[] internal = ref.extract(config);
        eval(ref.construct(internal));
        internal = mutate(internal);
        eval(ref.construct(internal));
        internal = mutate(internal);
        eval(ref.construct(internal));
    }
}