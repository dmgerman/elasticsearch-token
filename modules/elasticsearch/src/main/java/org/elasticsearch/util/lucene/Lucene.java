begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.lucene
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|lucene
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
name|analysis
operator|.
name|KeywordAnalyzer
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
name|analysis
operator|.
name|standard
operator|.
name|StandardAnalyzer
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
name|IndexWriter
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
name|TermDocs
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
name|*
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
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|gnu
operator|.
name|trove
operator|.
name|TIntArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|Lucene
specifier|public
class|class
name|Lucene
block|{
DECL|field|STANDARD_ANALYZER
specifier|public
specifier|static
specifier|final
name|StandardAnalyzer
name|STANDARD_ANALYZER
init|=
operator|new
name|StandardAnalyzer
argument_list|(
name|Version
operator|.
name|LUCENE_CURRENT
argument_list|)
decl_stmt|;
DECL|field|KEYWORD_ANALYZER
specifier|public
specifier|static
specifier|final
name|KeywordAnalyzer
name|KEYWORD_ANALYZER
init|=
operator|new
name|KeywordAnalyzer
argument_list|()
decl_stmt|;
DECL|field|NO_DOC
specifier|public
specifier|static
specifier|final
name|int
name|NO_DOC
init|=
operator|-
literal|1
decl_stmt|;
DECL|method|count
specifier|public
specifier|static
name|long
name|count
parameter_list|(
name|IndexSearcher
name|searcher
parameter_list|,
name|Query
name|query
parameter_list|,
name|float
name|minScore
parameter_list|)
throws|throws
name|IOException
block|{
name|CountCollector
name|countCollector
init|=
operator|new
name|CountCollector
argument_list|(
name|minScore
argument_list|)
decl_stmt|;
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|countCollector
argument_list|)
expr_stmt|;
return|return
name|countCollector
operator|.
name|count
argument_list|()
return|;
block|}
DECL|method|docId
specifier|public
specifier|static
name|int
name|docId
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|Term
name|term
parameter_list|)
throws|throws
name|IOException
block|{
name|TermDocs
name|termDocs
init|=
name|reader
operator|.
name|termDocs
argument_list|(
name|term
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|termDocs
operator|.
name|next
argument_list|()
condition|)
block|{
return|return
name|termDocs
operator|.
name|doc
argument_list|()
return|;
block|}
return|return
name|NO_DOC
return|;
block|}
finally|finally
block|{
name|termDocs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|docIds
specifier|public
specifier|static
name|TIntArrayList
name|docIds
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|Term
name|term
parameter_list|,
name|int
name|expectedSize
parameter_list|)
throws|throws
name|IOException
block|{
name|TermDocs
name|termDocs
init|=
name|reader
operator|.
name|termDocs
argument_list|(
name|term
argument_list|)
decl_stmt|;
name|TIntArrayList
name|list
init|=
operator|new
name|TIntArrayList
argument_list|(
name|expectedSize
argument_list|)
decl_stmt|;
try|try
block|{
while|while
condition|(
name|termDocs
operator|.
name|next
argument_list|()
condition|)
block|{
name|list
operator|.
name|add
argument_list|(
name|termDocs
operator|.
name|doc
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|termDocs
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
return|return
name|list
return|;
block|}
comment|/**      * Closes the index reader, returning<tt>false</tt> if it failed to close.      */
DECL|method|safeClose
specifier|public
specifier|static
name|boolean
name|safeClose
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
if|if
condition|(
name|reader
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
try|try
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
comment|/**      * Closes the index writer, returning<tt>false</tt> if it failed to close.      */
DECL|method|safeClose
specifier|public
specifier|static
name|boolean
name|safeClose
parameter_list|(
name|IndexWriter
name|writer
parameter_list|)
block|{
if|if
condition|(
name|writer
operator|==
literal|null
condition|)
block|{
return|return
literal|true
return|;
block|}
try|try
block|{
name|writer
operator|.
name|close
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
DECL|method|readTopDocs
specifier|public
specifier|static
name|TopDocs
name|readTopDocs
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
comment|// no docs
return|return
literal|null
return|;
block|}
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|int
name|totalHits
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|float
name|maxScore
init|=
name|in
operator|.
name|readFloat
argument_list|()
decl_stmt|;
name|SortField
index|[]
name|fields
init|=
operator|new
name|SortField
index|[
name|in
operator|.
name|readInt
argument_list|()
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
name|fields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|fields
index|[
name|i
index|]
operator|=
operator|new
name|SortField
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|,
name|in
operator|.
name|readInt
argument_list|()
argument_list|,
name|in
operator|.
name|readBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|FieldDoc
index|[]
name|fieldDocs
init|=
operator|new
name|FieldDoc
index|[
name|in
operator|.
name|readInt
argument_list|()
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
name|fieldDocs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Comparable
index|[]
name|cFields
init|=
operator|new
name|Comparable
index|[
name|in
operator|.
name|readInt
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|cFields
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|byte
name|type
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
literal|0
condition|)
block|{
name|cFields
index|[
name|j
index|]
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|1
condition|)
block|{
name|cFields
index|[
name|j
index|]
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|2
condition|)
block|{
name|cFields
index|[
name|j
index|]
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|3
condition|)
block|{
name|cFields
index|[
name|j
index|]
operator|=
name|in
operator|.
name|readFloat
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|4
condition|)
block|{
name|cFields
index|[
name|j
index|]
operator|=
name|in
operator|.
name|readDouble
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
literal|5
condition|)
block|{
name|cFields
index|[
name|j
index|]
operator|=
name|in
operator|.
name|readByte
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can't match type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
name|fieldDocs
index|[
name|i
index|]
operator|=
operator|new
name|FieldDoc
argument_list|(
name|in
operator|.
name|readInt
argument_list|()
argument_list|,
name|in
operator|.
name|readFloat
argument_list|()
argument_list|,
name|cFields
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TopFieldDocs
argument_list|(
name|totalHits
argument_list|,
name|fieldDocs
argument_list|,
name|fields
argument_list|,
name|maxScore
argument_list|)
return|;
block|}
else|else
block|{
name|int
name|totalHits
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|float
name|maxScore
init|=
name|in
operator|.
name|readFloat
argument_list|()
decl_stmt|;
name|ScoreDoc
index|[]
name|scoreDocs
init|=
operator|new
name|ScoreDoc
index|[
name|in
operator|.
name|readInt
argument_list|()
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
name|scoreDocs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|scoreDocs
index|[
name|i
index|]
operator|=
operator|new
name|ScoreDoc
argument_list|(
name|in
operator|.
name|readInt
argument_list|()
argument_list|,
name|in
operator|.
name|readFloat
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|TopDocs
argument_list|(
name|totalHits
argument_list|,
name|scoreDocs
argument_list|,
name|maxScore
argument_list|)
return|;
block|}
block|}
DECL|method|writeTopDocs
specifier|public
specifier|static
name|void
name|writeTopDocs
parameter_list|(
name|DataOutput
name|out
parameter_list|,
name|TopDocs
name|topDocs
parameter_list|,
name|int
name|from
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
operator|-
name|from
operator|<
literal|0
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
return|return;
block|}
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|topDocs
operator|instanceof
name|TopFieldDocs
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|TopFieldDocs
name|topFieldDocs
init|=
operator|(
name|TopFieldDocs
operator|)
name|topDocs
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|topDocs
operator|.
name|totalHits
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|topDocs
operator|.
name|getMaxScore
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|topFieldDocs
operator|.
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|SortField
name|sortField
range|:
name|topFieldDocs
operator|.
name|fields
control|)
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|sortField
operator|.
name|getField
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|sortField
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|sortField
operator|.
name|getReverse
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
operator|-
name|from
argument_list|)
expr_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ScoreDoc
name|doc
range|:
name|topFieldDocs
operator|.
name|scoreDocs
control|)
block|{
if|if
condition|(
name|index
operator|++
operator|<
name|from
condition|)
block|{
continue|continue;
block|}
name|FieldDoc
name|fieldDoc
init|=
operator|(
name|FieldDoc
operator|)
name|doc
decl_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|fieldDoc
operator|.
name|fields
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|Comparable
name|field
range|:
name|fieldDoc
operator|.
name|fields
control|)
block|{
name|Class
name|type
init|=
name|field
operator|.
name|getClass
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|String
operator|.
name|class
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
operator|(
name|String
operator|)
name|field
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Integer
operator|.
name|class
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
operator|(
name|Integer
operator|)
name|field
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Long
operator|.
name|class
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
operator|(
name|Long
operator|)
name|field
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Float
operator|.
name|class
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
operator|(
name|Float
operator|)
name|field
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Double
operator|.
name|class
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
operator|(
name|Double
operator|)
name|field
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Byte
operator|.
name|class
condition|)
block|{
name|out
operator|.
name|write
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
operator|(
name|Byte
operator|)
name|field
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Can't handle sort field value of type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|doc
operator|.
name|doc
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|doc
operator|.
name|score
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|topDocs
operator|.
name|totalHits
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|topDocs
operator|.
name|getMaxScore
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
operator|-
name|from
argument_list|)
expr_stmt|;
name|int
name|index
init|=
literal|0
decl_stmt|;
for|for
control|(
name|ScoreDoc
name|doc
range|:
name|topDocs
operator|.
name|scoreDocs
control|)
block|{
if|if
condition|(
name|index
operator|++
operator|<
name|from
condition|)
block|{
continue|continue;
block|}
name|out
operator|.
name|writeInt
argument_list|(
name|doc
operator|.
name|doc
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeFloat
argument_list|(
name|doc
operator|.
name|score
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|readExplanation
specifier|public
specifier|static
name|Explanation
name|readExplanation
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|float
name|value
init|=
name|in
operator|.
name|readFloat
argument_list|()
decl_stmt|;
name|String
name|description
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
name|Explanation
name|explanation
init|=
operator|new
name|Explanation
argument_list|(
name|value
argument_list|,
name|description
argument_list|)
decl_stmt|;
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|int
name|size
init|=
name|in
operator|.
name|readInt
argument_list|()
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
name|size
condition|;
name|i
operator|++
control|)
block|{
name|explanation
operator|.
name|addDetail
argument_list|(
name|readExplanation
argument_list|(
name|in
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|explanation
return|;
block|}
DECL|method|writeExplanation
specifier|public
specifier|static
name|void
name|writeExplanation
parameter_list|(
name|DataOutput
name|out
parameter_list|,
name|Explanation
name|explanation
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeFloat
argument_list|(
name|explanation
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|explanation
operator|.
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
name|Explanation
index|[]
name|subExplanations
init|=
name|explanation
operator|.
name|getDetails
argument_list|()
decl_stmt|;
if|if
condition|(
name|subExplanations
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeBoolean
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|subExplanations
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|Explanation
name|subExp
range|:
name|subExplanations
control|)
block|{
name|writeExplanation
argument_list|(
name|out
argument_list|,
name|subExp
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|class|CountCollector
specifier|public
specifier|static
class|class
name|CountCollector
extends|extends
name|Collector
block|{
DECL|field|minScore
specifier|private
specifier|final
name|float
name|minScore
decl_stmt|;
DECL|field|scorer
specifier|private
name|Scorer
name|scorer
decl_stmt|;
DECL|field|count
specifier|private
name|long
name|count
decl_stmt|;
DECL|method|CountCollector
specifier|public
name|CountCollector
parameter_list|(
name|float
name|minScore
parameter_list|)
block|{
name|this
operator|.
name|minScore
operator|=
name|minScore
expr_stmt|;
block|}
DECL|method|count
specifier|public
name|long
name|count
parameter_list|()
block|{
return|return
name|this
operator|.
name|count
return|;
block|}
DECL|method|setScorer
annotation|@
name|Override
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|scorer
operator|=
name|scorer
expr_stmt|;
block|}
DECL|method|collect
annotation|@
name|Override
specifier|public
name|void
name|collect
parameter_list|(
name|int
name|doc
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|scorer
operator|.
name|score
argument_list|()
operator|>
name|minScore
condition|)
block|{
name|count
operator|++
expr_stmt|;
block|}
block|}
DECL|method|setNextReader
annotation|@
name|Override
specifier|public
name|void
name|setNextReader
parameter_list|(
name|IndexReader
name|reader
parameter_list|,
name|int
name|docBase
parameter_list|)
throws|throws
name|IOException
block|{         }
DECL|method|acceptsDocsOutOfOrder
annotation|@
name|Override
specifier|public
name|boolean
name|acceptsDocsOutOfOrder
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
block|}
DECL|method|Lucene
specifier|private
name|Lucene
parameter_list|()
block|{      }
block|}
end_class

end_unit

