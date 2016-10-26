begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
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
name|ParseField
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
name|Locale
import|;
end_import

begin_comment
comment|/**  * The type of a script, more specifically where it gets loaded from:  * - provided dynamically at request time  * - loaded from an index  * - loaded from file  */
end_comment

begin_enum
DECL|enum|ScriptType
specifier|public
enum|enum
name|ScriptType
block|{
DECL|enum constant|INLINE
name|INLINE
argument_list|(
literal|0
argument_list|,
literal|"inline"
argument_list|,
literal|"inline"
argument_list|,
literal|false
argument_list|)
block|,
DECL|enum constant|STORED
name|STORED
argument_list|(
literal|1
argument_list|,
literal|"id"
argument_list|,
literal|"stored"
argument_list|,
literal|false
argument_list|)
block|,
DECL|enum constant|FILE
name|FILE
argument_list|(
literal|2
argument_list|,
literal|"file"
argument_list|,
literal|"file"
argument_list|,
literal|true
argument_list|)
block|;
DECL|field|val
specifier|private
specifier|final
name|int
name|val
decl_stmt|;
DECL|field|parseField
specifier|private
specifier|final
name|ParseField
name|parseField
decl_stmt|;
DECL|field|scriptType
specifier|private
specifier|final
name|String
name|scriptType
decl_stmt|;
DECL|field|defaultScriptEnabled
specifier|private
specifier|final
name|boolean
name|defaultScriptEnabled
decl_stmt|;
DECL|method|readFrom
specifier|public
specifier|static
name|ScriptType
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|scriptTypeVal
init|=
name|in
operator|.
name|readVInt
argument_list|()
decl_stmt|;
for|for
control|(
name|ScriptType
name|type
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|type
operator|.
name|val
operator|==
name|scriptTypeVal
condition|)
block|{
return|return
name|type
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unexpected value read for ScriptType got ["
operator|+
name|scriptTypeVal
operator|+
literal|"] expected one of ["
operator|+
name|INLINE
operator|.
name|val
operator|+
literal|","
operator|+
name|FILE
operator|.
name|val
operator|+
literal|","
operator|+
name|STORED
operator|.
name|val
operator|+
literal|"]"
argument_list|)
throw|;
block|}
DECL|method|writeTo
specifier|public
specifier|static
name|void
name|writeTo
parameter_list|(
name|ScriptType
name|scriptType
parameter_list|,
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|scriptType
operator|!=
literal|null
condition|)
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|scriptType
operator|.
name|val
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeVInt
argument_list|(
name|INLINE
operator|.
name|val
argument_list|)
expr_stmt|;
comment|//Default to inline
block|}
block|}
DECL|method|ScriptType
name|ScriptType
parameter_list|(
name|int
name|val
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|scriptType
parameter_list|,
name|boolean
name|defaultScriptEnabled
parameter_list|)
block|{
name|this
operator|.
name|val
operator|=
name|val
expr_stmt|;
name|this
operator|.
name|parseField
operator|=
operator|new
name|ParseField
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|this
operator|.
name|scriptType
operator|=
name|scriptType
expr_stmt|;
name|this
operator|.
name|defaultScriptEnabled
operator|=
name|defaultScriptEnabled
expr_stmt|;
block|}
DECL|method|getParseField
specifier|public
name|ParseField
name|getParseField
parameter_list|()
block|{
return|return
name|parseField
return|;
block|}
DECL|method|getDefaultScriptEnabled
specifier|public
name|boolean
name|getDefaultScriptEnabled
parameter_list|()
block|{
return|return
name|defaultScriptEnabled
return|;
block|}
DECL|method|getScriptType
specifier|public
name|String
name|getScriptType
parameter_list|()
block|{
return|return
name|scriptType
return|;
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
name|name
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
return|;
block|}
block|}
end_enum

end_unit

