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
name|Nullable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
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
name|common
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
name|common
operator|.
name|xcontent
operator|.
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentLocation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
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
name|internal
operator|.
name|SearchContext
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
comment|/**  *  */
end_comment

begin_class
DECL|class|SearchParseException
specifier|public
class|class
name|SearchParseException
extends|extends
name|SearchContextException
block|{
DECL|field|UNKNOWN_POSITION
specifier|public
specifier|static
specifier|final
name|int
name|UNKNOWN_POSITION
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|lineNumber
specifier|private
specifier|final
name|int
name|lineNumber
decl_stmt|;
DECL|field|columnNumber
specifier|private
specifier|final
name|int
name|columnNumber
decl_stmt|;
DECL|method|SearchParseException
specifier|public
name|SearchParseException
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|String
name|msg
parameter_list|,
annotation|@
name|Nullable
name|XContentLocation
name|location
parameter_list|)
block|{
name|this
argument_list|(
name|context
argument_list|,
name|msg
argument_list|,
name|location
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|SearchParseException
specifier|public
name|SearchParseException
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|String
name|msg
parameter_list|,
annotation|@
name|Nullable
name|XContentLocation
name|location
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|super
argument_list|(
name|context
argument_list|,
name|msg
argument_list|,
name|cause
argument_list|)
expr_stmt|;
name|int
name|lineNumber
init|=
name|UNKNOWN_POSITION
decl_stmt|;
name|int
name|columnNumber
init|=
name|UNKNOWN_POSITION
decl_stmt|;
if|if
condition|(
name|location
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|location
operator|!=
literal|null
condition|)
block|{
name|lineNumber
operator|=
name|location
operator|.
name|lineNumber
expr_stmt|;
name|columnNumber
operator|=
name|location
operator|.
name|columnNumber
expr_stmt|;
block|}
block|}
name|this
operator|.
name|columnNumber
operator|=
name|columnNumber
expr_stmt|;
name|this
operator|.
name|lineNumber
operator|=
name|lineNumber
expr_stmt|;
block|}
DECL|method|SearchParseException
specifier|public
name|SearchParseException
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|lineNumber
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
name|columnNumber
operator|=
name|in
operator|.
name|readInt
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
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
name|super
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|lineNumber
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|columnNumber
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|status
specifier|public
name|RestStatus
name|status
parameter_list|()
block|{
return|return
name|RestStatus
operator|.
name|BAD_REQUEST
return|;
block|}
annotation|@
name|Override
DECL|method|innerToXContent
specifier|protected
name|void
name|innerToXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|lineNumber
operator|!=
name|UNKNOWN_POSITION
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"line"
argument_list|,
name|lineNumber
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"col"
argument_list|,
name|columnNumber
argument_list|)
expr_stmt|;
block|}
name|super
operator|.
name|innerToXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
block|}
comment|/**      * Line number of the location of the error      *      * @return the line number or -1 if unknown      */
DECL|method|getLineNumber
specifier|public
name|int
name|getLineNumber
parameter_list|()
block|{
return|return
name|lineNumber
return|;
block|}
comment|/**      * Column number of the location of the error      *      * @return the column number or -1 if unknown      */
DECL|method|getColumnNumber
specifier|public
name|int
name|getColumnNumber
parameter_list|()
block|{
return|return
name|columnNumber
return|;
block|}
block|}
end_class

end_unit

