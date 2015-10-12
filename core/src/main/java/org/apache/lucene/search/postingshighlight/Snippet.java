begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.search.postingshighlight
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|postingshighlight
package|;
end_package

begin_comment
comment|/**  * Represents a scored highlighted snippet.  * It's our own arbitrary object that we get back from the postings highlighter when highlighting a document.  * Every snippet contains its formatted text and its score.  * The score is needed in case we want to sort snippets by score, they get sorted by position in the text by default.  */
end_comment

begin_class
DECL|class|Snippet
specifier|public
class|class
name|Snippet
block|{
DECL|field|text
specifier|private
specifier|final
name|String
name|text
decl_stmt|;
DECL|field|score
specifier|private
specifier|final
name|float
name|score
decl_stmt|;
DECL|field|isHighlighted
specifier|private
specifier|final
name|boolean
name|isHighlighted
decl_stmt|;
DECL|method|Snippet
specifier|public
name|Snippet
parameter_list|(
name|String
name|text
parameter_list|,
name|float
name|score
parameter_list|,
name|boolean
name|isHighlighted
parameter_list|)
block|{
name|this
operator|.
name|text
operator|=
name|text
expr_stmt|;
name|this
operator|.
name|score
operator|=
name|score
expr_stmt|;
name|this
operator|.
name|isHighlighted
operator|=
name|isHighlighted
expr_stmt|;
block|}
DECL|method|getText
specifier|public
name|String
name|getText
parameter_list|()
block|{
return|return
name|text
return|;
block|}
DECL|method|getScore
specifier|public
name|float
name|getScore
parameter_list|()
block|{
return|return
name|score
return|;
block|}
DECL|method|isHighlighted
specifier|public
name|boolean
name|isHighlighted
parameter_list|()
block|{
return|return
name|isHighlighted
return|;
block|}
block|}
end_class

end_unit

