begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.lucene.all
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|lucene
operator|.
name|all
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
name|Analyzer
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
name|TokenStream
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
name|document
operator|.
name|FieldType
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
name|IndexOptions
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|io
operator|.
name|Reader
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|AllField
specifier|public
class|class
name|AllField
extends|extends
name|Field
block|{
DECL|field|allEntries
specifier|private
specifier|final
name|AllEntries
name|allEntries
decl_stmt|;
DECL|field|analyzer
specifier|private
specifier|final
name|Analyzer
name|analyzer
decl_stmt|;
DECL|method|AllField
specifier|public
name|AllField
parameter_list|(
name|String
name|name
parameter_list|,
name|AllEntries
name|allEntries
parameter_list|,
name|Analyzer
name|analyzer
parameter_list|,
name|FieldType
name|fieldType
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|fieldType
argument_list|)
expr_stmt|;
name|this
operator|.
name|allEntries
operator|=
name|allEntries
expr_stmt|;
name|this
operator|.
name|analyzer
operator|=
name|analyzer
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|stringValue
specifier|public
name|String
name|stringValue
parameter_list|()
block|{
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
condition|)
block|{
return|return
name|allEntries
operator|.
name|buildText
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|readerValue
specifier|public
name|Reader
name|readerValue
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
comment|/** Returns the {@link AllEntries} containing the original text fields for the document. */
DECL|method|getAllEntries
specifier|public
name|AllEntries
name|getAllEntries
parameter_list|()
block|{
return|return
name|allEntries
return|;
block|}
annotation|@
name|Override
DECL|method|tokenStream
specifier|public
name|TokenStream
name|tokenStream
parameter_list|(
name|Analyzer
name|analyzer
parameter_list|,
name|TokenStream
name|previous
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|allEntries
operator|.
name|reset
argument_list|()
expr_stmt|;
comment|// reset the all entries, just in case it was read already
if|if
condition|(
name|allEntries
operator|.
name|customBoost
argument_list|()
operator|&&
name|fieldType
argument_list|()
operator|.
name|indexOptions
argument_list|()
operator|.
name|compareTo
argument_list|(
name|IndexOptions
operator|.
name|DOCS_AND_FREQS_AND_POSITIONS
argument_list|)
operator|>=
literal|0
condition|)
block|{
comment|// TODO: we should be able to reuse "previous" if its instanceof AllTokenStream?
comment|// but we need to be careful this optimization is safe (and tested)...
comment|// AllTokenStream maps boost to 4-byte payloads, so we only need to use it any field had non-default (!= 1.0f) boost and if
comment|// positions are indexed:
return|return
name|AllTokenStream
operator|.
name|allTokenStream
argument_list|(
name|name
argument_list|,
name|allEntries
argument_list|,
name|analyzer
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|analyzer
operator|.
name|tokenStream
argument_list|(
name|name
argument_list|,
name|allEntries
argument_list|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Failed to create token stream"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

