begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|highlight
operator|.
name|FastVectorHighlighter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|highlight
operator|.
name|Highlighter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|highlight
operator|.
name|PlainHighlighter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|highlight
operator|.
name|PostingsHighlighter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * An extensions point and registry for all the highlighters a node supports.  */
end_comment

begin_class
DECL|class|Highlighters
specifier|public
specifier|final
class|class
name|Highlighters
block|{
DECL|field|parsers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Highlighter
argument_list|>
name|parsers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|Highlighters
specifier|public
name|Highlighters
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|registerHighlighter
argument_list|(
literal|"fvh"
argument_list|,
operator|new
name|FastVectorHighlighter
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|registerHighlighter
argument_list|(
literal|"plain"
argument_list|,
operator|new
name|PlainHighlighter
argument_list|()
argument_list|)
expr_stmt|;
name|registerHighlighter
argument_list|(
literal|"postings"
argument_list|,
operator|new
name|PostingsHighlighter
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Returns the highlighter for the given key or<code>null</code> if there is no highlighter registered for that key.      */
DECL|method|get
specifier|public
name|Highlighter
name|get
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|parsers
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
comment|/**      * Registers a highlighter for the given key      * @param key the key the highlighter should be referenced by in the search request      * @param highlighter the highlighter instance      */
DECL|method|registerHighlighter
name|void
name|registerHighlighter
parameter_list|(
name|String
name|key
parameter_list|,
name|Highlighter
name|highlighter
parameter_list|)
block|{
if|if
condition|(
name|highlighter
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't register null highlighter for key: ["
operator|+
name|key
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|parsers
operator|.
name|putIfAbsent
argument_list|(
name|key
argument_list|,
name|highlighter
argument_list|)
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Can't register the same [highlighter] more than once for ["
operator|+
name|key
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

