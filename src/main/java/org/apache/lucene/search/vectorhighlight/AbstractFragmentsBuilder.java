begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.search.vectorhighlight
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|vectorhighlight
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
name|document
operator|.
name|Field
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
name|search
operator|.
name|highlight
operator|.
name|Encoder
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
name|*
import|;
end_import

begin_comment
comment|/**  * Abstract {@link FragmentsBuilder} implementation that detects whether highlight hits occurred on a field that is  * multivalued (Basically fields that have the same name) and splits the highlight snippets according to a single field  * boundary. This avoids that a highlight hit is shown as one hit whilst it is actually a hit on multiple fields.  */
end_comment

begin_class
DECL|class|AbstractFragmentsBuilder
specifier|public
specifier|abstract
class|class
name|AbstractFragmentsBuilder
extends|extends
name|BaseFragmentsBuilder
block|{
DECL|field|discreteMultiValueHighlighting
specifier|private
name|boolean
name|discreteMultiValueHighlighting
init|=
literal|true
decl_stmt|;
DECL|method|AbstractFragmentsBuilder
specifier|protected
name|AbstractFragmentsBuilder
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
DECL|method|AbstractFragmentsBuilder
specifier|protected
name|AbstractFragmentsBuilder
parameter_list|(
name|BoundaryScanner
name|boundaryScanner
parameter_list|)
block|{
name|super
argument_list|(
name|boundaryScanner
argument_list|)
expr_stmt|;
block|}
DECL|method|AbstractFragmentsBuilder
specifier|protected
name|AbstractFragmentsBuilder
parameter_list|(
name|String
index|[]
name|preTags
parameter_list|,
name|String
index|[]
name|postTags
parameter_list|)
block|{
name|super
argument_list|(
name|preTags
argument_list|,
name|postTags
argument_list|)
expr_stmt|;
block|}
DECL|method|AbstractFragmentsBuilder
specifier|public
name|AbstractFragmentsBuilder
parameter_list|(
name|String
index|[]
name|preTags
parameter_list|,
name|String
index|[]
name|postTags
parameter_list|,
name|BoundaryScanner
name|bs
parameter_list|)
block|{
name|super
argument_list|(
name|preTags
argument_list|,
name|postTags
argument_list|,
name|bs
argument_list|)
expr_stmt|;
block|}
DECL|method|setDiscreteMultiValueHighlighting
specifier|public
name|void
name|setDiscreteMultiValueHighlighting
parameter_list|(
name|boolean
name|discreteMultiValueHighlighting
parameter_list|)
block|{
name|this
operator|.
name|discreteMultiValueHighlighting
operator|=
name|discreteMultiValueHighlighting
expr_stmt|;
block|}
DECL|method|createFragments
specifier|public
name|String
index|[]
name|createFragments
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|int
name|docId
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|FieldFragList
name|fieldFragList
parameter_list|,
name|int
name|maxNumFragments
parameter_list|,
name|String
index|[]
name|preTags
parameter_list|,
name|String
index|[]
name|postTags
parameter_list|,
name|Encoder
name|encoder
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|maxNumFragments
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"maxNumFragments("
operator|+
name|maxNumFragments
operator|+
literal|") must be positive number."
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
argument_list|>
name|fragInfos
init|=
name|fieldFragList
operator|.
name|getFragInfos
argument_list|()
decl_stmt|;
name|Field
index|[]
name|values
init|=
name|getFields
argument_list|(
name|reader
argument_list|,
name|docId
argument_list|,
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|values
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
name|discreteMultiValueHighlighting
operator|&&
name|values
operator|.
name|length
operator|>
literal|1
condition|)
block|{
name|fragInfos
operator|=
name|discreteMultiValueHighlighting
argument_list|(
name|fragInfos
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
name|fragInfos
operator|=
name|getWeightedFragInfoList
argument_list|(
name|fragInfos
argument_list|)
expr_stmt|;
name|int
name|limitFragments
init|=
name|maxNumFragments
operator|<
name|fragInfos
operator|.
name|size
argument_list|()
condition|?
name|maxNumFragments
else|:
name|fragInfos
operator|.
name|size
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|fragments
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|limitFragments
argument_list|)
decl_stmt|;
name|StringBuilder
name|buffer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|int
index|[]
name|nextValueIndex
init|=
block|{
literal|0
block|}
decl_stmt|;
for|for
control|(
name|int
name|n
init|=
literal|0
init|;
name|n
operator|<
name|limitFragments
condition|;
name|n
operator|++
control|)
block|{
name|FieldFragList
operator|.
name|WeightedFragInfo
name|fragInfo
init|=
name|fragInfos
operator|.
name|get
argument_list|(
name|n
argument_list|)
decl_stmt|;
name|fragments
operator|.
name|add
argument_list|(
name|makeFragment
argument_list|(
name|buffer
argument_list|,
name|nextValueIndex
argument_list|,
name|values
argument_list|,
name|fragInfo
argument_list|,
name|preTags
argument_list|,
name|postTags
argument_list|,
name|encoder
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|fragments
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|fragments
operator|.
name|size
argument_list|()
index|]
argument_list|)
return|;
block|}
DECL|method|discreteMultiValueHighlighting
specifier|protected
name|List
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
argument_list|>
name|discreteMultiValueHighlighting
parameter_list|(
name|List
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
argument_list|>
name|fragInfos
parameter_list|,
name|Field
index|[]
name|fields
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
argument_list|>
argument_list|>
name|fieldNameToFragInfos
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|List
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
argument_list|>
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Field
name|field
range|:
name|fields
control|)
block|{
name|fieldNameToFragInfos
operator|.
name|put
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|,
operator|new
name|ArrayList
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|fragInfos
label|:
for|for
control|(
name|FieldFragList
operator|.
name|WeightedFragInfo
name|fragInfo
range|:
name|fragInfos
control|)
block|{
name|int
name|fieldStart
decl_stmt|;
name|int
name|fieldEnd
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Field
name|field
range|:
name|fields
control|)
block|{
if|if
condition|(
name|field
operator|.
name|stringValue
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|fieldEnd
operator|++
expr_stmt|;
continue|continue;
block|}
name|fieldStart
operator|=
name|fieldEnd
expr_stmt|;
name|fieldEnd
operator|+=
name|field
operator|.
name|stringValue
argument_list|()
operator|.
name|length
argument_list|()
operator|+
literal|1
expr_stmt|;
comment|// + 1 for going to next field with same name.
if|if
condition|(
name|fragInfo
operator|.
name|getStartOffset
argument_list|()
operator|>=
name|fieldStart
operator|&&
name|fragInfo
operator|.
name|getEndOffset
argument_list|()
operator|>=
name|fieldStart
operator|&&
name|fragInfo
operator|.
name|getStartOffset
argument_list|()
operator|<=
name|fieldEnd
operator|&&
name|fragInfo
operator|.
name|getEndOffset
argument_list|()
operator|<=
name|fieldEnd
condition|)
block|{
name|fieldNameToFragInfos
operator|.
name|get
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|fragInfo
argument_list|)
expr_stmt|;
continue|continue
name|fragInfos
continue|;
block|}
if|if
condition|(
name|fragInfo
operator|.
name|getSubInfos
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
continue|continue
name|fragInfos
continue|;
block|}
name|FieldPhraseList
operator|.
name|WeightedPhraseInfo
operator|.
name|Toffs
name|firstToffs
init|=
name|fragInfo
operator|.
name|getSubInfos
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getTermsOffsets
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
if|if
condition|(
name|fragInfo
operator|.
name|getStartOffset
argument_list|()
operator|>=
name|fieldEnd
operator|||
name|firstToffs
operator|.
name|getStartOffset
argument_list|()
operator|>=
name|fieldEnd
condition|)
block|{
continue|continue;
block|}
name|int
name|fragStart
init|=
name|fieldStart
decl_stmt|;
if|if
condition|(
name|fragInfo
operator|.
name|getStartOffset
argument_list|()
operator|>
name|fieldStart
operator|&&
name|fragInfo
operator|.
name|getStartOffset
argument_list|()
operator|<
name|fieldEnd
condition|)
block|{
name|fragStart
operator|=
name|fragInfo
operator|.
name|getStartOffset
argument_list|()
expr_stmt|;
block|}
name|int
name|fragEnd
init|=
name|fieldEnd
decl_stmt|;
if|if
condition|(
name|fragInfo
operator|.
name|getEndOffset
argument_list|()
operator|>
name|fieldStart
operator|&&
name|fragInfo
operator|.
name|getEndOffset
argument_list|()
operator|<
name|fieldEnd
condition|)
block|{
name|fragEnd
operator|=
name|fragInfo
operator|.
name|getEndOffset
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|WeightedFragInfo
operator|.
name|SubInfo
argument_list|>
name|subInfos
init|=
operator|new
name|ArrayList
argument_list|<
name|WeightedFragInfo
operator|.
name|SubInfo
argument_list|>
argument_list|()
decl_stmt|;
name|WeightedFragInfo
name|weightedFragInfo
init|=
operator|new
name|WeightedFragInfo
argument_list|(
name|fragStart
argument_list|,
name|fragEnd
argument_list|,
name|fragInfo
operator|.
name|getTotalBoost
argument_list|()
argument_list|,
name|subInfos
argument_list|)
decl_stmt|;
name|Iterator
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
operator|.
name|SubInfo
argument_list|>
name|subInfoIterator
init|=
name|fragInfo
operator|.
name|getSubInfos
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|subInfoIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|FieldFragList
operator|.
name|WeightedFragInfo
operator|.
name|SubInfo
name|subInfo
init|=
name|subInfoIterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|FieldPhraseList
operator|.
name|WeightedPhraseInfo
operator|.
name|Toffs
argument_list|>
name|toffsList
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldPhraseList
operator|.
name|WeightedPhraseInfo
operator|.
name|Toffs
argument_list|>
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|FieldPhraseList
operator|.
name|WeightedPhraseInfo
operator|.
name|Toffs
argument_list|>
name|toffsIterator
init|=
name|subInfo
operator|.
name|getTermsOffsets
argument_list|()
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|toffsIterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|FieldPhraseList
operator|.
name|WeightedPhraseInfo
operator|.
name|Toffs
name|toffs
init|=
name|toffsIterator
operator|.
name|next
argument_list|()
decl_stmt|;
if|if
condition|(
name|toffs
operator|.
name|getStartOffset
argument_list|()
operator|>=
name|fieldStart
operator|&&
name|toffs
operator|.
name|getEndOffset
argument_list|()
operator|<=
name|fieldEnd
condition|)
block|{
name|toffsList
operator|.
name|add
argument_list|(
name|toffs
argument_list|)
expr_stmt|;
name|toffsIterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|toffsList
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|subInfos
operator|.
name|add
argument_list|(
operator|new
name|FieldFragList
operator|.
name|WeightedFragInfo
operator|.
name|SubInfo
argument_list|(
name|subInfo
operator|.
name|getText
argument_list|()
argument_list|,
name|toffsList
argument_list|,
name|subInfo
operator|.
name|getSeqnum
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|subInfo
operator|.
name|getTermsOffsets
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|subInfoIterator
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
name|fieldNameToFragInfos
operator|.
name|get
argument_list|(
name|field
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|add
argument_list|(
name|weightedFragInfo
argument_list|)
expr_stmt|;
block|}
block|}
name|List
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|List
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
argument_list|>
name|weightedFragInfos
range|:
name|fieldNameToFragInfos
operator|.
name|values
argument_list|()
control|)
block|{
name|result
operator|.
name|addAll
argument_list|(
name|weightedFragInfos
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|result
argument_list|,
operator|new
name|Comparator
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
argument_list|>
argument_list|()
block|{
specifier|public
name|int
name|compare
parameter_list|(
name|FieldFragList
operator|.
name|WeightedFragInfo
name|info1
parameter_list|,
name|FieldFragList
operator|.
name|WeightedFragInfo
name|info2
parameter_list|)
block|{
return|return
name|info1
operator|.
name|getStartOffset
argument_list|()
operator|-
name|info2
operator|.
name|getStartOffset
argument_list|()
return|;
block|}
block|}
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
DECL|class|WeightedFragInfo
specifier|private
specifier|static
class|class
name|WeightedFragInfo
extends|extends
name|FieldFragList
operator|.
name|WeightedFragInfo
block|{
DECL|field|EMPTY
specifier|private
specifier|final
specifier|static
name|List
argument_list|<
name|FieldPhraseList
operator|.
name|WeightedPhraseInfo
argument_list|>
name|EMPTY
init|=
name|Collections
operator|.
name|emptyList
argument_list|()
decl_stmt|;
DECL|method|WeightedFragInfo
specifier|private
name|WeightedFragInfo
parameter_list|(
name|int
name|startOffset
parameter_list|,
name|int
name|endOffset
parameter_list|,
name|float
name|totalBoost
parameter_list|,
name|List
argument_list|<
name|FieldFragList
operator|.
name|WeightedFragInfo
operator|.
name|SubInfo
argument_list|>
name|subInfos
parameter_list|)
block|{
name|super
argument_list|(
name|startOffset
argument_list|,
name|endOffset
argument_list|,
name|subInfos
argument_list|,
name|totalBoost
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

