package com.mzt.logapi.util.diff;

import de.danielbechler.diff.access.Accessor;
import de.danielbechler.diff.access.Instances;
import de.danielbechler.diff.comparison.ComparisonStrategy;
import de.danielbechler.diff.comparison.ComparisonStrategyResolver;
import de.danielbechler.diff.differ.Differ;
import de.danielbechler.diff.differ.DifferDispatcher;
import de.danielbechler.diff.identity.IdentityStrategy;
import de.danielbechler.diff.identity.IdentityStrategyResolver;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.util.Assert;

import java.util.*;

/**
 * @author wulang
 **/
public class ArrayDiffer implements Differ {
    private final DifferDispatcher differDispatcher;
    private final ComparisonStrategyResolver comparisonStrategyResolver;
    private final IdentityStrategyResolver identityStrategyResolver;

    public ArrayDiffer(DifferDispatcher differDispatcher, ComparisonStrategyResolver comparisonStrategyResolver, IdentityStrategyResolver identityStrategyResolver) {
        Assert.notNull(differDispatcher, "differDispatcher");
        this.differDispatcher = differDispatcher;
        Assert.notNull(comparisonStrategyResolver, "comparisonStrategyResolver");
        this.comparisonStrategyResolver = comparisonStrategyResolver;
        Assert.notNull(identityStrategyResolver, "identityStrategyResolver");
        this.identityStrategyResolver = identityStrategyResolver;
    }

    @Override
    public boolean accepts(final Class<?> type) {
        return !type.isPrimitive() && type.isArray();
    }

    @Override
    public final DiffNode compare(final DiffNode parentNode, final Instances collectionInstances) {
        final DiffNode collectionNode = newNode(parentNode, collectionInstances);
        final IdentityStrategy identityStrategy = identityStrategyResolver.resolveIdentityStrategy(collectionNode);
        if (identityStrategy != null) {
            collectionNode.setChildIdentityStrategy(identityStrategy);
        }
        if (collectionInstances.hasBeenAdded()) {
            final Collection<?> addedItems = findCollection(collectionInstances.getWorking());
            compareItems(collectionNode, collectionInstances, addedItems, identityStrategy);
            collectionNode.setState(DiffNode.State.ADDED);
        } else if (collectionInstances.hasBeenRemoved()) {
            final Collection<?> removedItems = findCollection(collectionInstances.getBase());
            compareItems(collectionNode, collectionInstances, removedItems, identityStrategy);
            collectionNode.setState(DiffNode.State.REMOVED);
        } else if (collectionInstances.areSame()) {
            collectionNode.setState(DiffNode.State.UNTOUCHED);
        } else {
            final ComparisonStrategy comparisonStrategy = comparisonStrategyResolver.resolveComparisonStrategy(collectionNode);
            if (comparisonStrategy == null) {
                compareInternally(collectionNode, collectionInstances, identityStrategy);
            } else {
                compareUsingComparisonStrategy(collectionNode, collectionInstances, comparisonStrategy);
            }
        }
        return collectionNode;
    }

    private Collection<?> findCollection(Object source) {
        return source == null ? new ArrayList<>() : new LinkedList<>(Arrays.asList((Object[]) source));
    }

    private static DiffNode newNode(final DiffNode parentNode,
                                    final Instances collectionInstances) {
        final Accessor accessor = collectionInstances.getSourceAccessor();
        final Class<?> type = collectionInstances.getType();
        return new DiffNode(parentNode, accessor, type);
    }

    private void compareItems(final DiffNode collectionNode,
                              final Instances collectionInstances,
                              final Iterable<?> items,
                              final IdentityStrategy identityStrategy) {
        for (final Object item : items) {
            final Accessor itemAccessor = new ArrayItemAccessor(item, identityStrategy);
            differDispatcher.dispatch(collectionNode, collectionInstances, itemAccessor);
        }
    }

    private void compareInternally(final DiffNode collectionNode,
                                   final Instances collectionInstances,
                                   final IdentityStrategy identityStrategy) {
        final Collection<?> working = Arrays.asList((Object[]) collectionInstances.getWorking());
        final Collection<?> base = Arrays.asList((Object[]) collectionInstances.getBase());

        final Iterable<?> added = new LinkedList<Object>(working);
        final Iterable<?> removed = new LinkedList<Object>(base);
        final Iterable<?> known = new LinkedList<Object>(base);

        remove(added, base, identityStrategy);
        remove(removed, working, identityStrategy);
        remove(known, added, identityStrategy);
        remove(known, removed, identityStrategy);

        compareItems(collectionNode, collectionInstances, added, identityStrategy);
        compareItems(collectionNode, collectionInstances, removed, identityStrategy);
        compareItems(collectionNode, collectionInstances, known, identityStrategy);
    }

    private static void compareUsingComparisonStrategy(final DiffNode collectionNode,
                                                       final Instances collectionInstances,
                                                       final ComparisonStrategy comparisonStrategy) {
        comparisonStrategy.compare(collectionNode,
                collectionInstances.getType(),
                collectionInstances.getWorking(Collection.class),
                collectionInstances.getBase(Collection.class));
    }

    private void remove(final Iterable<?> from, final Iterable<?> these, final IdentityStrategy identityStrategy) {
        final Iterator<?> iterator = from.iterator();
        while (iterator.hasNext()) {
            final Object item = iterator.next();
            if (contains(these, item, identityStrategy)) {
                iterator.remove();
            }
        }
    }

    private boolean contains(final Iterable<?> haystack, final Object needle, final IdentityStrategy identityStrategy) {
        for (final Object item : haystack) {
            if (identityStrategy.equals(needle, item)) {
                return true;
            }
        }
        return false;
    }
}
