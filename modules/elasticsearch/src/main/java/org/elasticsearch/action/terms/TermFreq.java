begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.terms
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|terms
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Streamable
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
name|Comparator
import|;
end_import

begin_comment
comment|/**  * A tuple of term and its document frequency (in how many documents this term exists).  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TermFreq
specifier|public
class|class
name|TermFreq
implements|implements
name|Streamable
block|{
comment|/**      * A frequency based comparator with higher frequencies first.      */
DECL|field|freqComparator
specifier|private
specifier|static
specifier|final
name|Comparator
argument_list|<
name|TermFreq
argument_list|>
name|freqComparator
init|=
operator|new
name|Comparator
argument_list|<
name|TermFreq
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|TermFreq
name|o1
parameter_list|,
name|TermFreq
name|o2
parameter_list|)
block|{
name|int
name|i
init|=
name|o2
operator|.
name|docFreq
argument_list|()
operator|-
name|o1
operator|.
name|docFreq
argument_list|()
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|i
operator|=
name|o1
operator|.
name|term
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|term
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
block|}
decl_stmt|;
comment|/**      * Lexical based comparator.      */
DECL|field|termComparator
specifier|private
specifier|static
specifier|final
name|Comparator
argument_list|<
name|TermFreq
argument_list|>
name|termComparator
init|=
operator|new
name|Comparator
argument_list|<
name|TermFreq
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
name|TermFreq
name|o1
parameter_list|,
name|TermFreq
name|o2
parameter_list|)
block|{
name|int
name|i
init|=
name|o1
operator|.
name|term
argument_list|()
operator|.
name|compareTo
argument_list|(
name|o2
operator|.
name|term
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
literal|0
condition|)
block|{
name|i
operator|=
name|o1
operator|.
name|docFreq
argument_list|()
operator|-
name|o2
operator|.
name|docFreq
argument_list|()
expr_stmt|;
block|}
return|return
name|i
return|;
block|}
block|}
decl_stmt|;
comment|/**      * A frequency based comparator with higher frequencies first.      */
DECL|method|freqComparator
specifier|public
specifier|static
name|Comparator
argument_list|<
name|TermFreq
argument_list|>
name|freqComparator
parameter_list|()
block|{
return|return
name|freqComparator
return|;
block|}
comment|/**      * Lexical based comparator.      */
DECL|method|termComparator
specifier|public
specifier|static
name|Comparator
argument_list|<
name|TermFreq
argument_list|>
name|termComparator
parameter_list|()
block|{
return|return
name|termComparator
return|;
block|}
DECL|field|term
specifier|private
name|String
name|term
decl_stmt|;
DECL|field|docFreq
specifier|private
name|int
name|docFreq
decl_stmt|;
DECL|method|TermFreq
specifier|private
name|TermFreq
parameter_list|()
block|{      }
comment|/**      * Constructs a new term freq.      *      * @param term    The term      * @param docFreq The document frequency      */
DECL|method|TermFreq
name|TermFreq
parameter_list|(
name|String
name|term
parameter_list|,
name|int
name|docFreq
parameter_list|)
block|{
name|this
operator|.
name|term
operator|=
name|term
expr_stmt|;
name|this
operator|.
name|docFreq
operator|=
name|docFreq
expr_stmt|;
block|}
comment|/**      * The term.      */
DECL|method|term
specifier|public
name|String
name|term
parameter_list|()
block|{
return|return
name|term
return|;
block|}
comment|/**      * The document frequency of the term (in how many documents this term exists).      */
DECL|method|docFreq
specifier|public
name|int
name|docFreq
parameter_list|()
block|{
return|return
name|docFreq
return|;
block|}
DECL|method|readTermFreq
specifier|public
specifier|static
name|TermFreq
name|readTermFreq
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|TermFreq
name|termFreq
init|=
operator|new
name|TermFreq
argument_list|()
decl_stmt|;
name|termFreq
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
return|return
name|termFreq
return|;
block|}
DECL|method|readFrom
annotation|@
name|Override
specifier|public
name|void
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|term
operator|=
name|in
operator|.
name|readUTF
argument_list|()
expr_stmt|;
name|docFreq
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
block|}
DECL|method|writeTo
annotation|@
name|Override
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|term
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|docFreq
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

