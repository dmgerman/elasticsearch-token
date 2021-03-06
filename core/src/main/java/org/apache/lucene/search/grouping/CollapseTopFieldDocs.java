begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.search.grouping
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|grouping
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|FieldComparator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|FieldDoc
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|ScoreDoc
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Sort
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|SortField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|TopFieldDocs
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|PriorityQueue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_comment
comment|/**  * Represents hits returned by {@link CollapsingTopDocsCollector#getTopDocs()}.  */
end_comment

begin_class
DECL|class|CollapseTopFieldDocs
specifier|public
specifier|final
class|class
name|CollapseTopFieldDocs
extends|extends
name|TopFieldDocs
block|{
comment|/** The field used for collapsing **/
DECL|field|field
specifier|public
specifier|final
name|String
name|field
decl_stmt|;
comment|/** The collapse value for each top doc */
DECL|field|collapseValues
specifier|public
specifier|final
name|Object
index|[]
name|collapseValues
decl_stmt|;
DECL|method|CollapseTopFieldDocs
specifier|public
name|CollapseTopFieldDocs
parameter_list|(
name|String
name|field
parameter_list|,
name|int
name|totalHits
parameter_list|,
name|ScoreDoc
index|[]
name|scoreDocs
parameter_list|,
name|SortField
index|[]
name|sortFields
parameter_list|,
name|Object
index|[]
name|values
parameter_list|,
name|float
name|maxScore
parameter_list|)
block|{
name|super
argument_list|(
name|totalHits
argument_list|,
name|scoreDocs
argument_list|,
name|sortFields
argument_list|,
name|maxScore
argument_list|)
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|collapseValues
operator|=
name|values
expr_stmt|;
block|}
comment|// Refers to one hit:
DECL|class|ShardRef
specifier|private
specifier|static
specifier|final
class|class
name|ShardRef
block|{
comment|// Which shard (index into shardHits[]):
DECL|field|shardIndex
specifier|final
name|int
name|shardIndex
decl_stmt|;
comment|// True if we should use the incoming ScoreDoc.shardIndex for sort order
DECL|field|useScoreDocIndex
specifier|final
name|boolean
name|useScoreDocIndex
decl_stmt|;
comment|// Which hit within the shard:
DECL|field|hitIndex
name|int
name|hitIndex
decl_stmt|;
DECL|method|ShardRef
name|ShardRef
parameter_list|(
name|int
name|shardIndex
parameter_list|,
name|boolean
name|useScoreDocIndex
parameter_list|)
block|{
name|this
operator|.
name|shardIndex
operator|=
name|shardIndex
expr_stmt|;
name|this
operator|.
name|useScoreDocIndex
operator|=
name|useScoreDocIndex
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"ShardRef(shardIndex="
operator|+
name|shardIndex
operator|+
literal|" hitIndex="
operator|+
name|hitIndex
operator|+
literal|")"
return|;
block|}
DECL|method|getShardIndex
name|int
name|getShardIndex
parameter_list|(
name|ScoreDoc
name|scoreDoc
parameter_list|)
block|{
if|if
condition|(
name|useScoreDocIndex
condition|)
block|{
if|if
condition|(
name|scoreDoc
operator|.
name|shardIndex
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"setShardIndex is false but TopDocs["
operator|+
name|shardIndex
operator|+
literal|"].scoreDocs["
operator|+
name|hitIndex
operator|+
literal|"] is not set"
argument_list|)
throw|;
block|}
return|return
name|scoreDoc
operator|.
name|shardIndex
return|;
block|}
else|else
block|{
comment|// NOTE: we don't assert that shardIndex is -1 here, because caller could in fact have set it but asked us to ignore it now
return|return
name|shardIndex
return|;
block|}
block|}
block|}
comment|/**      * if we need to tie-break since score / sort value are the same we first compare shard index (lower shard wins)      * and then iff shard index is the same we use the hit index.      */
DECL|method|tieBreakLessThan
specifier|static
name|boolean
name|tieBreakLessThan
parameter_list|(
name|ShardRef
name|first
parameter_list|,
name|ScoreDoc
name|firstDoc
parameter_list|,
name|ShardRef
name|second
parameter_list|,
name|ScoreDoc
name|secondDoc
parameter_list|)
block|{
specifier|final
name|int
name|firstShardIndex
init|=
name|first
operator|.
name|getShardIndex
argument_list|(
name|firstDoc
argument_list|)
decl_stmt|;
specifier|final
name|int
name|secondShardIndex
init|=
name|second
operator|.
name|getShardIndex
argument_list|(
name|secondDoc
argument_list|)
decl_stmt|;
comment|// Tie break: earlier shard wins
if|if
condition|(
name|firstShardIndex
operator|<
name|secondShardIndex
condition|)
block|{
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|firstShardIndex
operator|>
name|secondShardIndex
condition|)
block|{
return|return
literal|false
return|;
block|}
else|else
block|{
comment|// Tie break in same shard: resolve however the
comment|// shard had resolved it:
assert|assert
name|first
operator|.
name|hitIndex
operator|!=
name|second
operator|.
name|hitIndex
assert|;
return|return
name|first
operator|.
name|hitIndex
operator|<
name|second
operator|.
name|hitIndex
return|;
block|}
block|}
DECL|class|MergeSortQueue
specifier|private
specifier|static
class|class
name|MergeSortQueue
extends|extends
name|PriorityQueue
argument_list|<
name|ShardRef
argument_list|>
block|{
comment|// These are really FieldDoc instances:
DECL|field|shardHits
specifier|final
name|ScoreDoc
index|[]
index|[]
name|shardHits
decl_stmt|;
DECL|field|comparators
specifier|final
name|FieldComparator
argument_list|<
name|?
argument_list|>
index|[]
name|comparators
decl_stmt|;
DECL|field|reverseMul
specifier|final
name|int
index|[]
name|reverseMul
decl_stmt|;
DECL|method|MergeSortQueue
name|MergeSortQueue
parameter_list|(
name|Sort
name|sort
parameter_list|,
name|CollapseTopFieldDocs
index|[]
name|shardHits
parameter_list|)
block|{
name|super
argument_list|(
name|shardHits
operator|.
name|length
argument_list|)
expr_stmt|;
name|this
operator|.
name|shardHits
operator|=
operator|new
name|ScoreDoc
index|[
name|shardHits
operator|.
name|length
index|]
index|[]
expr_stmt|;
for|for
control|(
name|int
name|shardIDX
init|=
literal|0
init|;
name|shardIDX
operator|<
name|shardHits
operator|.
name|length
condition|;
name|shardIDX
operator|++
control|)
block|{
specifier|final
name|ScoreDoc
index|[]
name|shard
init|=
name|shardHits
index|[
name|shardIDX
index|]
operator|.
name|scoreDocs
decl_stmt|;
if|if
condition|(
name|shard
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|shardHits
index|[
name|shardIDX
index|]
operator|=
name|shard
expr_stmt|;
comment|// Fail gracefully if API is misused:
for|for
control|(
name|int
name|hitIDX
init|=
literal|0
init|;
name|hitIDX
operator|<
name|shard
operator|.
name|length
condition|;
name|hitIDX
operator|++
control|)
block|{
specifier|final
name|ScoreDoc
name|sd
init|=
name|shard
index|[
name|hitIDX
index|]
decl_stmt|;
specifier|final
name|FieldDoc
name|gd
init|=
operator|(
name|FieldDoc
operator|)
name|sd
decl_stmt|;
assert|assert
name|gd
operator|.
name|fields
operator|!=
literal|null
assert|;
block|}
block|}
block|}
specifier|final
name|SortField
index|[]
name|sortFields
init|=
name|sort
operator|.
name|getSort
argument_list|()
decl_stmt|;
name|comparators
operator|=
operator|new
name|FieldComparator
index|[
name|sortFields
operator|.
name|length
index|]
expr_stmt|;
name|reverseMul
operator|=
operator|new
name|int
index|[
name|sortFields
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|compIDX
init|=
literal|0
init|;
name|compIDX
operator|<
name|sortFields
operator|.
name|length
condition|;
name|compIDX
operator|++
control|)
block|{
specifier|final
name|SortField
name|sortField
init|=
name|sortFields
index|[
name|compIDX
index|]
decl_stmt|;
name|comparators
index|[
name|compIDX
index|]
operator|=
name|sortField
operator|.
name|getComparator
argument_list|(
literal|1
argument_list|,
name|compIDX
argument_list|)
expr_stmt|;
name|reverseMul
index|[
name|compIDX
index|]
operator|=
name|sortField
operator|.
name|getReverse
argument_list|()
condition|?
operator|-
literal|1
else|:
literal|1
expr_stmt|;
block|}
block|}
comment|// Returns true if first is< second
annotation|@
name|Override
DECL|method|lessThan
specifier|public
name|boolean
name|lessThan
parameter_list|(
name|ShardRef
name|first
parameter_list|,
name|ShardRef
name|second
parameter_list|)
block|{
assert|assert
name|first
operator|!=
name|second
assert|;
specifier|final
name|FieldDoc
name|firstFD
init|=
operator|(
name|FieldDoc
operator|)
name|shardHits
index|[
name|first
operator|.
name|shardIndex
index|]
index|[
name|first
operator|.
name|hitIndex
index|]
decl_stmt|;
specifier|final
name|FieldDoc
name|secondFD
init|=
operator|(
name|FieldDoc
operator|)
name|shardHits
index|[
name|second
operator|.
name|shardIndex
index|]
index|[
name|second
operator|.
name|hitIndex
index|]
decl_stmt|;
for|for
control|(
name|int
name|compIDX
init|=
literal|0
init|;
name|compIDX
operator|<
name|comparators
operator|.
name|length
condition|;
name|compIDX
operator|++
control|)
block|{
specifier|final
name|FieldComparator
name|comp
init|=
name|comparators
index|[
name|compIDX
index|]
decl_stmt|;
specifier|final
name|int
name|cmp
init|=
name|reverseMul
index|[
name|compIDX
index|]
operator|*
name|comp
operator|.
name|compareValues
argument_list|(
name|firstFD
operator|.
name|fields
index|[
name|compIDX
index|]
argument_list|,
name|secondFD
operator|.
name|fields
index|[
name|compIDX
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
operator|<
literal|0
return|;
block|}
block|}
return|return
name|tieBreakLessThan
argument_list|(
name|first
argument_list|,
name|firstFD
argument_list|,
name|second
argument_list|,
name|secondFD
argument_list|)
return|;
block|}
block|}
comment|/**      * Returns a new CollapseTopDocs, containing topN collapsed results across      * the provided CollapseTopDocs, sorting by score. Each {@link CollapseTopFieldDocs} instance must be sorted.      **/
DECL|method|merge
specifier|public
specifier|static
name|CollapseTopFieldDocs
name|merge
parameter_list|(
name|Sort
name|sort
parameter_list|,
name|int
name|start
parameter_list|,
name|int
name|size
parameter_list|,
name|CollapseTopFieldDocs
index|[]
name|shardHits
parameter_list|,
name|boolean
name|setShardIndex
parameter_list|)
block|{
name|String
name|collapseField
init|=
name|shardHits
index|[
literal|0
index|]
operator|.
name|field
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|shardHits
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|collapseField
operator|.
name|equals
argument_list|(
name|shardHits
index|[
name|i
index|]
operator|.
name|field
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"collapse field differ across shards ["
operator|+
name|collapseField
operator|+
literal|"] != ["
operator|+
name|shardHits
index|[
name|i
index|]
operator|.
name|field
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
specifier|final
name|PriorityQueue
argument_list|<
name|ShardRef
argument_list|>
name|queue
init|=
operator|new
name|MergeSortQueue
argument_list|(
name|sort
argument_list|,
name|shardHits
argument_list|)
decl_stmt|;
name|int
name|totalHitCount
init|=
literal|0
decl_stmt|;
name|int
name|availHitCount
init|=
literal|0
decl_stmt|;
name|float
name|maxScore
init|=
name|Float
operator|.
name|MIN_VALUE
decl_stmt|;
for|for
control|(
name|int
name|shardIDX
init|=
literal|0
init|;
name|shardIDX
operator|<
name|shardHits
operator|.
name|length
condition|;
name|shardIDX
operator|++
control|)
block|{
specifier|final
name|CollapseTopFieldDocs
name|shard
init|=
name|shardHits
index|[
name|shardIDX
index|]
decl_stmt|;
comment|// totalHits can be non-zero even if no hits were
comment|// collected, when searchAfter was used:
name|totalHitCount
operator|+=
name|shard
operator|.
name|totalHits
expr_stmt|;
if|if
condition|(
name|shard
operator|.
name|scoreDocs
operator|!=
literal|null
operator|&&
name|shard
operator|.
name|scoreDocs
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|availHitCount
operator|+=
name|shard
operator|.
name|scoreDocs
operator|.
name|length
expr_stmt|;
name|queue
operator|.
name|add
argument_list|(
operator|new
name|ShardRef
argument_list|(
name|shardIDX
argument_list|,
name|setShardIndex
operator|==
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|maxScore
operator|=
name|Math
operator|.
name|max
argument_list|(
name|maxScore
argument_list|,
name|shard
operator|.
name|getMaxScore
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|availHitCount
operator|==
literal|0
condition|)
block|{
name|maxScore
operator|=
name|Float
operator|.
name|NaN
expr_stmt|;
block|}
specifier|final
name|ScoreDoc
index|[]
name|hits
decl_stmt|;
specifier|final
name|Object
index|[]
name|values
decl_stmt|;
if|if
condition|(
name|availHitCount
operator|<=
name|start
condition|)
block|{
name|hits
operator|=
operator|new
name|ScoreDoc
index|[
literal|0
index|]
expr_stmt|;
name|values
operator|=
operator|new
name|Object
index|[
literal|0
index|]
expr_stmt|;
block|}
else|else
block|{
name|List
argument_list|<
name|ScoreDoc
argument_list|>
name|hitList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|collapseList
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|requestedResultWindow
init|=
name|start
operator|+
name|size
decl_stmt|;
name|int
name|numIterOnHits
init|=
name|Math
operator|.
name|min
argument_list|(
name|availHitCount
argument_list|,
name|requestedResultWindow
argument_list|)
decl_stmt|;
name|int
name|hitUpto
init|=
literal|0
decl_stmt|;
name|Set
argument_list|<
name|Object
argument_list|>
name|seen
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
name|hitUpto
operator|<
name|numIterOnHits
condition|)
block|{
if|if
condition|(
name|queue
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
break|break;
block|}
name|ShardRef
name|ref
init|=
name|queue
operator|.
name|top
argument_list|()
decl_stmt|;
specifier|final
name|ScoreDoc
name|hit
init|=
name|shardHits
index|[
name|ref
operator|.
name|shardIndex
index|]
operator|.
name|scoreDocs
index|[
name|ref
operator|.
name|hitIndex
index|]
decl_stmt|;
specifier|final
name|Object
name|collapseValue
init|=
name|shardHits
index|[
name|ref
operator|.
name|shardIndex
index|]
operator|.
name|collapseValues
index|[
name|ref
operator|.
name|hitIndex
operator|++
index|]
decl_stmt|;
if|if
condition|(
name|seen
operator|.
name|contains
argument_list|(
name|collapseValue
argument_list|)
condition|)
block|{
if|if
condition|(
name|ref
operator|.
name|hitIndex
operator|<
name|shardHits
index|[
name|ref
operator|.
name|shardIndex
index|]
operator|.
name|scoreDocs
operator|.
name|length
condition|)
block|{
name|queue
operator|.
name|updateTop
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|queue
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
continue|continue;
block|}
name|seen
operator|.
name|add
argument_list|(
name|collapseValue
argument_list|)
expr_stmt|;
if|if
condition|(
name|setShardIndex
condition|)
block|{
name|hit
operator|.
name|shardIndex
operator|=
name|ref
operator|.
name|shardIndex
expr_stmt|;
block|}
if|if
condition|(
name|hitUpto
operator|>=
name|start
condition|)
block|{
name|hitList
operator|.
name|add
argument_list|(
name|hit
argument_list|)
expr_stmt|;
name|collapseList
operator|.
name|add
argument_list|(
name|collapseValue
argument_list|)
expr_stmt|;
block|}
name|hitUpto
operator|++
expr_stmt|;
if|if
condition|(
name|ref
operator|.
name|hitIndex
operator|<
name|shardHits
index|[
name|ref
operator|.
name|shardIndex
index|]
operator|.
name|scoreDocs
operator|.
name|length
condition|)
block|{
comment|// Not done with this these TopDocs yet:
name|queue
operator|.
name|updateTop
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|queue
operator|.
name|pop
argument_list|()
expr_stmt|;
block|}
block|}
name|hits
operator|=
name|hitList
operator|.
name|toArray
argument_list|(
operator|new
name|ScoreDoc
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
name|values
operator|=
name|collapseList
operator|.
name|toArray
argument_list|(
operator|new
name|Object
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|CollapseTopFieldDocs
argument_list|(
name|collapseField
argument_list|,
name|totalHitCount
argument_list|,
name|hits
argument_list|,
name|sort
operator|.
name|getSort
argument_list|()
argument_list|,
name|values
argument_list|,
name|maxScore
argument_list|)
return|;
block|}
block|}
end_class

end_unit

