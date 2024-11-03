package com.zeljko.abstractive.zsv.manager.transport.model

/*
 OBJ_COMMIT = 1, OBJ_TREE = 2, OBJ_BLOB = 3, OBJ_TAG = 4
 OBJ_OFS_DELTA = 6, OBJ_REF_DELTA = 7
*/
enum class GitObjectType(val type: Int) {

    COMMIT(1),
    TREE(2),
    BLOB(3),
    TAG(4),
    OFS_DELTA(6),
    REF_DELTA(7);
    companion object {
        fun fromType(type: Int): GitObjectType {
            return when (type) {
                1 -> COMMIT
                2 -> TREE
                3 -> BLOB
                4 -> TAG
                6 -> OFS_DELTA
                7 -> REF_DELTA
                else -> throw IllegalArgumentException("Invalid type $type")
            }
        }
    }
}