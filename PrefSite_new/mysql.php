<?php

function openMysqlConnection()
{
	$__dbName__ = '700677';
	$__userName__ = '700677';
	$__password__ = 'egavga1995';
	$__host__ = 'localhost';

    $link = mysql_connect($__host__, $__userName__, $__password__);

    if (!$link) {
        // log info about error
        return NULL;
    }
    mysql_select_db($__dbName__);
    return $link;
}

function closeMysqlConnection($link)
{
    mysql_close($link);
}

?>