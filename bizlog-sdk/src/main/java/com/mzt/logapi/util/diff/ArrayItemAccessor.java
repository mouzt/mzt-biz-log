package com.mzt.logapi.util.diff;

import de.danielbechler.diff.access.Accessor;
import de.danielbechler.diff.access.TypeAwareAccessor;
import de.danielbechler.diff.identity.EqualsIdentityStrategy;
import de.danielbechler.diff.identity.IdentityStrategy;
import de.danielbechler.diff.selector.CollectionItemElementSelector;
import de.danielbechler.diff.selector.ElementSelector;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author wulang
 **/
public class ArrayItemAccessor implements TypeAwareAccessor, Accessor {

    private final Object referenceItem;
    private final IdentityStrategy identityStrategy;

    public ArrayItemAccessor(final Object referenceItem) {
        this(referenceItem, EqualsIdentityStrategy.getInstance());
    }

    public ArrayItemAccessor(final Object referenceItem,
                             final IdentityStrategy identityStrategy) {
        Assert.notNull(identityStrategy, "identityStrategy");
        this.referenceItem = referenceItem;
        this.identityStrategy = identityStrategy;
    }

    @Override
    public Class<?> getType() {
        return referenceItem != null ? referenceItem.getClass() : null;
    }

    @Override
    public String toString() {
        return "collection item " + getElementSelector();
    }

    @Override
    public ElementSelector getElementSelector() {
        final CollectionItemElementSelector selector = new CollectionItemElementSelector(referenceItem);
        return identityStrategy == null ? selector : selector.copyWithIdentityStrategy(identityStrategy);
    }

    @Override
    public Object get(final Object target) {
        final Collection targetCollection = objectAsCollection(target);
        if (targetCollection == null) {
            return null;
        }
        for (final Object item : targetCollection) {
            if (item != null && identityStrategy.equals(item, referenceItem)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void set(final Object target, final Object value) {
        final Collection<Object> targetCollection = objectAsCollection(target);
        if (targetCollection == null) {
            return;
        }
        final Object previous = get(target);
        if (previous != null) {
            unset(target);
        }
        targetCollection.add(value);
    }

    @SuppressWarnings("unchecked")
    private static Collection<Object> objectAsCollection(final Object object) {
        if (object == null) {
            return null;
        } else if (object.getClass().isArray()) {
            return new ArrayList<>(Arrays.asList((Object[]) object));
        }
        throw new IllegalArgumentException(object.getClass().toString());
    }

    @Override
    public void unset(final Object target) {
        final Collection<?> targetCollection = objectAsCollection(target);
        if (targetCollection == null) {
            return;
        }
        final Iterator<?> iterator = targetCollection.iterator();
        while (iterator.hasNext()) {
            final Object item = iterator.next();
            if (item != null && identityStrategy.equals(item, referenceItem)) {
                iterator.remove();
                break;
            }
        }
    }
}
