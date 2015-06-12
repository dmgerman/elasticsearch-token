begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.queries
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|queries
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|primitives
operator|.
name|Ints
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
name|index
operator|.
name|IndexReader
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
name|index
operator|.
name|IndexReaderContext
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
name|index
operator|.
name|LeafReaderContext
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
name|index
operator|.
name|Term
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
name|index
operator|.
name|TermContext
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
name|index
operator|.
name|TermState
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
name|BooleanClause
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
name|BooleanQuery
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
name|DisjunctionMaxQuery
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
name|Query
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
name|TermQuery
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
name|ArrayUtil
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
name|InPlaceMergeSorter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
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

begin_comment
comment|/**  * BlendedTermQuery can be used to unify term statistics across  * one or more fields in the index. A common problem with structured  * documents is that a term that is significant in on field might not be  * significant in other fields like in a scenario where documents represent  * users with a "first_name" and a "second_name". When someone searches  * for "simon" it will very likely get "paul simon" first since "simon" is a  * an uncommon last name ie. has a low document frequency. This query  * tries to "lie" about the global statistics like document frequency as well  * total term frequency to rank based on the estimated statistics.  *<p>  * While aggregating the total term frequency is trivial since it  * can be summed up not every {@link org.apache.lucene.search.similarities.Similarity}  * makes use of this statistic. The document frequency which is used in the  * {@link org.apache.lucene.search.similarities.DefaultSimilarity}  * can only be estimated as an lower-bound since it is a document based statistic. For  * the document frequency the maximum frequency across all fields per term is used  * which is the minimum number of documents the terms occurs in.  *</p>  */
end_comment

begin_comment
comment|// TODO maybe contribute to Lucene
end_comment

begin_class
DECL|class|BlendedTermQuery
specifier|public
specifier|abstract
class|class
name|BlendedTermQuery
extends|extends
name|Query
block|{
DECL|field|terms
specifier|private
specifier|final
name|Term
index|[]
name|terms
decl_stmt|;
DECL|method|BlendedTermQuery
specifier|public
name|BlendedTermQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|)
block|{
if|if
condition|(
name|terms
operator|==
literal|null
operator|||
name|terms
operator|.
name|length
operator|==
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"terms must not be null or empty"
argument_list|)
throw|;
block|}
name|this
operator|.
name|terms
operator|=
name|terms
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|rewrite
specifier|public
name|Query
name|rewrite
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|IndexReaderContext
name|context
init|=
name|reader
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|TermContext
index|[]
name|ctx
init|=
operator|new
name|TermContext
index|[
name|terms
operator|.
name|length
index|]
decl_stmt|;
name|int
index|[]
name|docFreqs
init|=
operator|new
name|int
index|[
name|ctx
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|terms
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ctx
index|[
name|i
index|]
operator|=
name|TermContext
operator|.
name|build
argument_list|(
name|context
argument_list|,
name|terms
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|docFreqs
index|[
name|i
index|]
operator|=
name|ctx
index|[
name|i
index|]
operator|.
name|docFreq
argument_list|()
expr_stmt|;
block|}
specifier|final
name|int
name|maxDoc
init|=
name|reader
operator|.
name|maxDoc
argument_list|()
decl_stmt|;
name|blend
argument_list|(
name|ctx
argument_list|,
name|maxDoc
argument_list|,
name|reader
argument_list|)
expr_stmt|;
name|Query
name|query
init|=
name|topLevelQuery
argument_list|(
name|terms
argument_list|,
name|ctx
argument_list|,
name|docFreqs
argument_list|,
name|maxDoc
argument_list|)
decl_stmt|;
name|query
operator|.
name|setBoost
argument_list|(
name|getBoost
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|query
return|;
block|}
DECL|method|topLevelQuery
specifier|protected
specifier|abstract
name|Query
name|topLevelQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|,
name|TermContext
index|[]
name|ctx
parameter_list|,
name|int
index|[]
name|docFreqs
parameter_list|,
name|int
name|maxDoc
parameter_list|)
function_decl|;
DECL|method|blend
specifier|protected
name|void
name|blend
parameter_list|(
specifier|final
name|TermContext
index|[]
name|contexts
parameter_list|,
name|int
name|maxDoc
parameter_list|,
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|contexts
operator|.
name|length
operator|<=
literal|1
condition|)
block|{
return|return;
block|}
name|int
name|max
init|=
literal|0
decl_stmt|;
name|long
name|minSumTTF
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|contexts
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|TermContext
name|ctx
init|=
name|contexts
index|[
name|i
index|]
decl_stmt|;
name|int
name|df
init|=
name|ctx
operator|.
name|docFreq
argument_list|()
decl_stmt|;
comment|// we use the max here since it's the only "true" estimation we can make here
comment|// at least max(df) documents have that term. Sum or Averages don't seem
comment|// to have a significant meaning here.
comment|// TODO: Maybe it could also make sense to assume independent distributions of documents and eg. have:
comment|//   df = df1 + df2 - (df1 * df2 / maxDoc)?
name|max
operator|=
name|Math
operator|.
name|max
argument_list|(
name|df
argument_list|,
name|max
argument_list|)
expr_stmt|;
if|if
condition|(
name|minSumTTF
operator|!=
operator|-
literal|1
operator|&&
name|ctx
operator|.
name|totalTermFreq
argument_list|()
operator|!=
operator|-
literal|1
condition|)
block|{
comment|// we need to find out the minimum sumTTF to adjust the statistics
comment|// otherwise the statistics don't match
name|minSumTTF
operator|=
name|Math
operator|.
name|min
argument_list|(
name|minSumTTF
argument_list|,
name|reader
operator|.
name|getSumTotalTermFreq
argument_list|(
name|terms
index|[
name|i
index|]
operator|.
name|field
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|minSumTTF
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
if|if
condition|(
name|minSumTTF
operator|!=
operator|-
literal|1
operator|&&
name|maxDoc
operator|>
name|minSumTTF
condition|)
block|{
name|maxDoc
operator|=
operator|(
name|int
operator|)
name|minSumTTF
expr_stmt|;
block|}
if|if
condition|(
name|max
operator|==
literal|0
condition|)
block|{
return|return;
comment|// we are done that term doesn't exist at all
block|}
name|long
name|sumTTF
init|=
name|minSumTTF
operator|==
operator|-
literal|1
condition|?
operator|-
literal|1
else|:
literal|0
decl_stmt|;
specifier|final
name|int
index|[]
name|tieBreak
init|=
operator|new
name|int
index|[
name|contexts
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|tieBreak
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|tieBreak
index|[
name|i
index|]
operator|=
name|i
expr_stmt|;
block|}
operator|new
name|InPlaceMergeSorter
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|void
name|swap
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
specifier|final
name|int
name|tmp
init|=
name|tieBreak
index|[
name|i
index|]
decl_stmt|;
name|tieBreak
index|[
name|i
index|]
operator|=
name|tieBreak
index|[
name|j
index|]
expr_stmt|;
name|tieBreak
index|[
name|j
index|]
operator|=
name|tmp
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|int
name|compare
parameter_list|(
name|int
name|i
parameter_list|,
name|int
name|j
parameter_list|)
block|{
return|return
name|Ints
operator|.
name|compare
argument_list|(
name|contexts
index|[
name|tieBreak
index|[
name|j
index|]
index|]
operator|.
name|docFreq
argument_list|()
argument_list|,
name|contexts
index|[
name|tieBreak
index|[
name|i
index|]
index|]
operator|.
name|docFreq
argument_list|()
argument_list|)
return|;
block|}
block|}
operator|.
name|sort
argument_list|(
literal|0
argument_list|,
name|tieBreak
operator|.
name|length
argument_list|)
expr_stmt|;
name|int
name|prev
init|=
name|contexts
index|[
name|tieBreak
index|[
literal|0
index|]
index|]
operator|.
name|docFreq
argument_list|()
decl_stmt|;
name|int
name|actualDf
init|=
name|Math
operator|.
name|min
argument_list|(
name|maxDoc
argument_list|,
name|max
argument_list|)
decl_stmt|;
assert|assert
name|actualDf
operator|>=
literal|0
operator|:
literal|"DF must be>= 0"
assert|;
comment|// here we try to add a little bias towards
comment|// the more popular (more frequent) fields
comment|// that acts as a tie breaker
for|for
control|(
name|int
name|i
range|:
name|tieBreak
control|)
block|{
name|TermContext
name|ctx
init|=
name|contexts
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|ctx
operator|.
name|docFreq
argument_list|()
operator|==
literal|0
condition|)
block|{
break|break;
block|}
specifier|final
name|int
name|current
init|=
name|ctx
operator|.
name|docFreq
argument_list|()
decl_stmt|;
if|if
condition|(
name|prev
operator|>
name|current
condition|)
block|{
name|actualDf
operator|++
expr_stmt|;
block|}
name|contexts
index|[
name|i
index|]
operator|=
name|ctx
operator|=
name|adjustDF
argument_list|(
name|ctx
argument_list|,
name|Math
operator|.
name|min
argument_list|(
name|maxDoc
argument_list|,
name|actualDf
argument_list|)
argument_list|)
expr_stmt|;
name|prev
operator|=
name|current
expr_stmt|;
if|if
condition|(
name|sumTTF
operator|>=
literal|0
operator|&&
name|ctx
operator|.
name|totalTermFreq
argument_list|()
operator|>=
literal|0
condition|)
block|{
name|sumTTF
operator|+=
name|ctx
operator|.
name|totalTermFreq
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|sumTTF
operator|=
operator|-
literal|1
expr_stmt|;
comment|// omit once TF is omitted anywhere!
block|}
block|}
name|sumTTF
operator|=
name|Math
operator|.
name|min
argument_list|(
name|sumTTF
argument_list|,
name|minSumTTF
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|contexts
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|int
name|df
init|=
name|contexts
index|[
name|i
index|]
operator|.
name|docFreq
argument_list|()
decl_stmt|;
if|if
condition|(
name|df
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
comment|// the blended sumTTF can't be greater than the sumTTTF on the field
specifier|final
name|long
name|fixedTTF
init|=
name|sumTTF
operator|==
operator|-
literal|1
condition|?
operator|-
literal|1
else|:
name|sumTTF
decl_stmt|;
name|contexts
index|[
name|i
index|]
operator|=
name|adjustTTF
argument_list|(
name|contexts
index|[
name|i
index|]
argument_list|,
name|fixedTTF
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|adjustTTF
specifier|private
name|TermContext
name|adjustTTF
parameter_list|(
name|TermContext
name|termContext
parameter_list|,
name|long
name|sumTTF
parameter_list|)
block|{
if|if
condition|(
name|sumTTF
operator|==
operator|-
literal|1
operator|&&
name|termContext
operator|.
name|totalTermFreq
argument_list|()
operator|==
operator|-
literal|1
condition|)
block|{
return|return
name|termContext
return|;
block|}
name|TermContext
name|newTermContext
init|=
operator|new
name|TermContext
argument_list|(
name|termContext
operator|.
name|topReaderContext
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|leaves
init|=
name|termContext
operator|.
name|topReaderContext
operator|.
name|leaves
argument_list|()
decl_stmt|;
specifier|final
name|int
name|len
decl_stmt|;
if|if
condition|(
name|leaves
operator|==
literal|null
condition|)
block|{
name|len
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|len
operator|=
name|leaves
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|int
name|df
init|=
name|termContext
operator|.
name|docFreq
argument_list|()
decl_stmt|;
name|long
name|ttf
init|=
name|sumTTF
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|len
condition|;
name|i
operator|++
control|)
block|{
name|TermState
name|termState
init|=
name|termContext
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|termState
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|newTermContext
operator|.
name|register
argument_list|(
name|termState
argument_list|,
name|i
argument_list|,
name|df
argument_list|,
name|ttf
argument_list|)
expr_stmt|;
name|df
operator|=
literal|0
expr_stmt|;
name|ttf
operator|=
literal|0
expr_stmt|;
block|}
return|return
name|newTermContext
return|;
block|}
DECL|method|adjustDF
specifier|private
specifier|static
name|TermContext
name|adjustDF
parameter_list|(
name|TermContext
name|ctx
parameter_list|,
name|int
name|newDocFreq
parameter_list|)
block|{
comment|// Use a value of ttf that is consistent with the doc freq (ie. gte)
name|long
name|newTTF
decl_stmt|;
if|if
condition|(
name|ctx
operator|.
name|totalTermFreq
argument_list|()
operator|<
literal|0
condition|)
block|{
name|newTTF
operator|=
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|newTTF
operator|=
name|Math
operator|.
name|max
argument_list|(
name|ctx
operator|.
name|totalTermFreq
argument_list|()
argument_list|,
name|newDocFreq
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|LeafReaderContext
argument_list|>
name|leaves
init|=
name|ctx
operator|.
name|topReaderContext
operator|.
name|leaves
argument_list|()
decl_stmt|;
specifier|final
name|int
name|len
decl_stmt|;
if|if
condition|(
name|leaves
operator|==
literal|null
condition|)
block|{
name|len
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|len
operator|=
name|leaves
operator|.
name|size
argument_list|()
expr_stmt|;
block|}
name|TermContext
name|newCtx
init|=
operator|new
name|TermContext
argument_list|(
name|ctx
operator|.
name|topReaderContext
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|len
condition|;
operator|++
name|i
control|)
block|{
name|TermState
name|termState
init|=
name|ctx
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|termState
operator|==
literal|null
condition|)
block|{
continue|continue;
block|}
name|newCtx
operator|.
name|register
argument_list|(
name|termState
argument_list|,
name|i
argument_list|,
name|newDocFreq
argument_list|,
name|newTTF
argument_list|)
expr_stmt|;
name|newDocFreq
operator|=
literal|0
expr_stmt|;
name|newTTF
operator|=
literal|0
expr_stmt|;
block|}
return|return
name|newCtx
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
literal|"blended(terms: "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|terms
argument_list|)
operator|+
literal|")"
return|;
block|}
DECL|field|equalTerms
specifier|private
specifier|volatile
name|Term
index|[]
name|equalTerms
init|=
literal|null
decl_stmt|;
DECL|method|equalsTerms
specifier|private
name|Term
index|[]
name|equalsTerms
parameter_list|()
block|{
if|if
condition|(
name|terms
operator|.
name|length
operator|==
literal|1
condition|)
block|{
return|return
name|terms
return|;
block|}
if|if
condition|(
name|equalTerms
operator|==
literal|null
condition|)
block|{
comment|// sort the terms to make sure equals and hashCode are consistent
comment|// this should be a very small cost and equivalent to a HashSet but less object creation
specifier|final
name|Term
index|[]
name|t
init|=
operator|new
name|Term
index|[
name|terms
operator|.
name|length
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|terms
argument_list|,
literal|0
argument_list|,
name|t
argument_list|,
literal|0
argument_list|,
name|terms
operator|.
name|length
argument_list|)
expr_stmt|;
name|ArrayUtil
operator|.
name|timSort
argument_list|(
name|t
argument_list|)
expr_stmt|;
name|equalTerms
operator|=
name|t
expr_stmt|;
block|}
return|return
name|equalTerms
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
if|if
condition|(
name|this
operator|==
name|o
condition|)
return|return
literal|true
return|;
if|if
condition|(
name|o
operator|==
literal|null
operator|||
name|getClass
argument_list|()
operator|!=
name|o
operator|.
name|getClass
argument_list|()
condition|)
return|return
literal|false
return|;
if|if
condition|(
operator|!
name|super
operator|.
name|equals
argument_list|(
name|o
argument_list|)
condition|)
return|return
literal|false
return|;
name|BlendedTermQuery
name|that
init|=
operator|(
name|BlendedTermQuery
operator|)
name|o
decl_stmt|;
if|if
condition|(
operator|!
name|Arrays
operator|.
name|equals
argument_list|(
name|equalsTerms
argument_list|()
argument_list|,
name|that
operator|.
name|equalsTerms
argument_list|()
argument_list|)
condition|)
return|return
literal|false
return|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|super
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|=
literal|31
operator|*
name|result
operator|+
name|Arrays
operator|.
name|hashCode
argument_list|(
name|equalsTerms
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
DECL|method|booleanBlendedQuery
specifier|public
specifier|static
name|BlendedTermQuery
name|booleanBlendedQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|,
specifier|final
name|boolean
name|disableCoord
parameter_list|)
block|{
return|return
name|booleanBlendedQuery
argument_list|(
name|terms
argument_list|,
literal|null
argument_list|,
name|disableCoord
argument_list|)
return|;
block|}
DECL|method|booleanBlendedQuery
specifier|public
specifier|static
name|BlendedTermQuery
name|booleanBlendedQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|,
specifier|final
name|float
index|[]
name|boosts
parameter_list|,
specifier|final
name|boolean
name|disableCoord
parameter_list|)
block|{
return|return
operator|new
name|BlendedTermQuery
argument_list|(
name|terms
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Query
name|topLevelQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|,
name|TermContext
index|[]
name|ctx
parameter_list|,
name|int
index|[]
name|docFreqs
parameter_list|,
name|int
name|maxDoc
parameter_list|)
block|{
name|BooleanQuery
name|query
init|=
operator|new
name|BooleanQuery
argument_list|(
name|disableCoord
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|terms
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|TermQuery
name|termQuery
init|=
operator|new
name|TermQuery
argument_list|(
name|terms
index|[
name|i
index|]
argument_list|,
name|ctx
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|boosts
operator|!=
literal|null
condition|)
block|{
name|termQuery
operator|.
name|setBoost
argument_list|(
name|boosts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|add
argument_list|(
name|termQuery
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
block|}
return|;
block|}
DECL|method|commonTermsBlendedQuery
specifier|public
specifier|static
name|BlendedTermQuery
name|commonTermsBlendedQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|,
specifier|final
name|float
index|[]
name|boosts
parameter_list|,
specifier|final
name|boolean
name|disableCoord
parameter_list|,
specifier|final
name|float
name|maxTermFrequency
parameter_list|)
block|{
return|return
operator|new
name|BlendedTermQuery
argument_list|(
name|terms
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Query
name|topLevelQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|,
name|TermContext
index|[]
name|ctx
parameter_list|,
name|int
index|[]
name|docFreqs
parameter_list|,
name|int
name|maxDoc
parameter_list|)
block|{
name|BooleanQuery
name|query
init|=
operator|new
name|BooleanQuery
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|BooleanQuery
name|high
init|=
operator|new
name|BooleanQuery
argument_list|(
name|disableCoord
argument_list|)
decl_stmt|;
name|BooleanQuery
name|low
init|=
operator|new
name|BooleanQuery
argument_list|(
name|disableCoord
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|terms
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|TermQuery
name|termQuery
init|=
operator|new
name|TermQuery
argument_list|(
name|terms
index|[
name|i
index|]
argument_list|,
name|ctx
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|boosts
operator|!=
literal|null
condition|)
block|{
name|termQuery
operator|.
name|setBoost
argument_list|(
name|boosts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
operator|(
name|maxTermFrequency
operator|>=
literal|1f
operator|&&
name|docFreqs
index|[
name|i
index|]
operator|>
name|maxTermFrequency
operator|)
operator|||
operator|(
name|docFreqs
index|[
name|i
index|]
operator|>
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
name|maxTermFrequency
operator|*
operator|(
name|float
operator|)
name|maxDoc
argument_list|)
operator|)
condition|)
block|{
name|high
operator|.
name|add
argument_list|(
name|termQuery
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|low
operator|.
name|add
argument_list|(
name|termQuery
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|low
operator|.
name|clauses
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|BooleanClause
name|booleanClause
range|:
name|high
control|)
block|{
name|booleanClause
operator|.
name|setOccur
argument_list|(
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
expr_stmt|;
block|}
return|return
name|high
return|;
block|}
elseif|else
if|if
condition|(
name|high
operator|.
name|clauses
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|low
return|;
block|}
else|else
block|{
name|query
operator|.
name|add
argument_list|(
name|high
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
expr_stmt|;
name|query
operator|.
name|add
argument_list|(
name|low
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
expr_stmt|;
return|return
name|query
return|;
block|}
block|}
block|}
return|;
block|}
DECL|method|dismaxBlendedQuery
specifier|public
specifier|static
name|BlendedTermQuery
name|dismaxBlendedQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|,
specifier|final
name|float
name|tieBreakerMultiplier
parameter_list|)
block|{
return|return
name|dismaxBlendedQuery
argument_list|(
name|terms
argument_list|,
literal|null
argument_list|,
name|tieBreakerMultiplier
argument_list|)
return|;
block|}
DECL|method|dismaxBlendedQuery
specifier|public
specifier|static
name|BlendedTermQuery
name|dismaxBlendedQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|,
specifier|final
name|float
index|[]
name|boosts
parameter_list|,
specifier|final
name|float
name|tieBreakerMultiplier
parameter_list|)
block|{
return|return
operator|new
name|BlendedTermQuery
argument_list|(
name|terms
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Query
name|topLevelQuery
parameter_list|(
name|Term
index|[]
name|terms
parameter_list|,
name|TermContext
index|[]
name|ctx
parameter_list|,
name|int
index|[]
name|docFreqs
parameter_list|,
name|int
name|maxDoc
parameter_list|)
block|{
name|DisjunctionMaxQuery
name|query
init|=
operator|new
name|DisjunctionMaxQuery
argument_list|(
name|tieBreakerMultiplier
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|terms
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|TermQuery
name|termQuery
init|=
operator|new
name|TermQuery
argument_list|(
name|terms
index|[
name|i
index|]
argument_list|,
name|ctx
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|boosts
operator|!=
literal|null
condition|)
block|{
name|termQuery
operator|.
name|setBoost
argument_list|(
name|boosts
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|query
operator|.
name|add
argument_list|(
name|termQuery
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
block|}
return|;
block|}
block|}
end_class

end_unit
